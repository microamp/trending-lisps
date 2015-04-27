(ns trending-lisps.scraper
  (:require [net.cgrand.enlive-html :as html]))

(def ^:dynamic *base-url* "https://github.com/trending")

(defn trim-text [text]
  (-> text
      clojure.string/trim
      clojure.string/trim-newline))

(defn starred? [text]
  (not (nil? (re-find #"\sstars?\stoday" text))))

(defn fetch-body [lang]
  (html/html-resource (java.net.URL. (str *base-url* "?l=" lang))))

(defn select-repos [body]
  (html/select body [:li.repo-list-item]))

(defn select-content [repo selectors]
  (let [texts (html/select repo (conj selectors html/text-node))]
    (if (empty? texts)
      ""
      (apply str (map trim-text texts)))))

(defn select-repo-name [repo]
  (select-content repo [:h3.repo-list-name]))

(defn select-repo-desc [repo]
  (select-content repo [:p.repo-list-description]))

(defn select-repo-meta [repo]
  (select-content repo [:p.repo-list-meta]))

(defn build-repo-map [repo]
  (let [name (select-repo-name repo)
        desc (select-repo-desc repo)
        meta (select-repo-meta repo)
        starred (starred? meta)]
    {:name name
     :desc desc
     :meta meta
     :starred starred}))

(defn build-repo-maps [lang]
  (map build-repo-map (-> lang
                          fetch-body
                          select-repos)))

(defn build-repo-maps-starred-only [lang]
  (->> lang
       build-repo-maps
       (filter (fn [repo-map] (:starred repo-map)))))
