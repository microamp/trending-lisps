(ns trending-lisps.core
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :as async :refer [chan go >! <! close!]]
            [overtone.at-at :as at-at :refer [mk-pool]]
            [trending-lisps.scraper :as scraper]
            [trending-lisps.edn :as edn]
            [trending-lisps.cache :as cache]
            [trending-lisps.twit-helper :as twit-helper]))

(def check-frequency (* 1000 60)) ; 60 seconds

(def cfg (edn/read-file "config.edn"))

(def my-pool (mk-pool))

(defn cached? [repo]
  (not (nil? (cache/get-cache (:name repo)))))

(defn update-cache [repo]
  (cache/set-cache (:name repo) repo))

(defn produce-lang-repos [ch-lang-repos langs]
  (doseq [lang (keys langs)]
    (go (let [lang-repos
              (map (fn [r] (assoc r :lang (get langs lang)))
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
            (let [cached? (cached? repo)]
              (do
                (update-cache repo)
                (if cached?
                  (log/debug (:name repo) "is starred but not marked as trending")
                  (twit-helper/twit-repo (:name repo) (:desc repo)))))))
        (recur))))

(defn -main []
  (let [ch-lang-repos (chan)
        ch-repos (chan)
        langs (:langs cfg)]
    (do
      ;; communication via two channels
      ;; channel 1: a collection of repos of a particular lang
      ;; channel 2: an individual repo
      (consume-lang-repos ch-lang-repos ch-repos)
      (consume-repos ch-repos)
      (at-at/every check-frequency
                   (partial produce-lang-repos
                            ch-lang-repos
                            langs)
                   my-pool))))
