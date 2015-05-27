(ns trending-lisps.twitter
  (:require [clojure.tools.logging :as log]
            [trending-lisps.edn :as edn]
            [twitter.api.restful :as twitter]
            [twitter.oauth :as twitter-oauth]
            [environ.core :refer [env]]))

(def status-limit 140)
(def base-url "https://github.com")
(def twitter-creds
  (twitter-oauth/make-oauth-creds (env :twitter-app-consumer-key)
                                  (env :twitter-app-consumer-secret)
                                  (env :twitter-user-access-token)
                                  (env :twitter-user-access-secret)))

(defn get-short-name [name]
  (second (clojure.string/split name #"/")))

(defn build-status [lang name desc name-short]
  (let [link (str base-url "/" name)
        status (str name-short " - " desc " " link)]
    (if (> (count status) status-limit)
      (str "[" lang "] " name-short " " link)
      status)))

(defn twit-repo [lang name desc twit?]
  (let [status (build-status lang name desc (get-short-name name))]
    (try
      (do (log/debug "twitting:" status)
          (when twit?
            (twitter/statuses-update :oauth-creds twitter-creds
                                     :params {:status status})))
      (catch Exception e
        (log/error "error:" (.getMessage e))))))
