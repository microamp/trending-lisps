(ns trending-lisps.core
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :as async :refer [chan go >! <! close!]]
            [overtone.at-at :as at-at :refer [mk-pool]]
            [trending-lisps.scraper :as scraper]
            [trending-lisps.edn :as edn]
            [trending-lisps.cache :as cache]
            [trending-lisps.twitter :as twitter]))

(def cfg (edn/read-file "config.edn"))
(def my-pool (mk-pool))

(defn cached? [repo]
  (not (nil? (cache/get-cache (:name repo)))))

(defn update-cache [repo expire]
  (cache/set-cache (:name repo) repo expire))

(defn produce-lang-repos [ch-lang-repos langs]
  (doseq [lang (keys langs)]
    (go (let [lang-repos (map (fn [r] (assoc r :lang (get langs lang)))
                              (scraper/build-repo-maps-starred-only lang))]
          (>! ch-lang-repos lang-repos)))))

(defn consume-lang-repos [ch-lang-repos ch-repos]
  (go (loop []
        (let [lang-repos (<! ch-lang-repos)]
          (doseq [lang-repo lang-repos]
            (>! ch-repos lang-repo)))
        (recur))))

(defn consume-repos [ch-repos]
  (go (loop []
        (let [repo (<! ch-repos)]
          (if (not (nil? repo))
            (try
              (let [cached? (cached? repo)]
                (do (update-cache repo (:cache-expire-after cfg))
                    (if cached?
                      (log/debug (:name repo) "is starred but not marked as trending")
                      (twitter/twit-repo (:lang repo)
                                         (:name repo)
                                         (:desc repo)
                                         (:twit? cfg)))))
              (catch Exception e
                (log/error "error:" (.getMessage e))))))
        (recur))))

(defn -main [& args]
  (let [ch-lang-repos (chan)
        ch-repos (chan)
        langs (:langs cfg)]
    (do
      ;; communication via two channels
      ;; channel 1: a collection of repos of a particular lang
      ;; channel 2: an individual repo
      (consume-lang-repos ch-lang-repos ch-repos)
      (consume-repos ch-repos)
      (at-at/every (:scrape-every cfg)
                   (partial produce-lang-repos
                            ch-lang-repos
                            langs)
                   my-pool))))
