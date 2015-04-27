(ns trending-lisps.core
  (:require [clojure.core.async :as async :refer [chan go >! <! close!]]
            [overtone.at-at :as at-at :refer [mk-pool]]
            [trending-lisps.scraper :as scraper]
            [trending-lisps.edn :as edn]
            [trending-lisps.cache :as cache]))

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
                  (prn (str (:name repo) " is not trending"))
                  (prn (str (:name repo) " is trending")))))))
        (recur))))

(defn post-trending-repos [ch-lang-repos ch-repos langs]
  ;; communication via two channels
  ;; channel 1: a collection of repos of a particular lang
  ;; channel 2: an individual repo
  (produce-lang-repos ch-lang-repos langs)
  (consume-lang-repos ch-lang-repos ch-repos)
  (consume-repos ch-repos))

(defn -main []
  (let [ch-lang-repos (chan)
        ch-repos (chan)
        langs (:langs cfg)]
    (at-at/every check-frequency
                 (partial post-trending-repos
                          ch-lang-repos
                          ch-repos
                          langs)
                 my-pool)))
