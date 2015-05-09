(ns trending-lisps.twitter
  (:require [clojure.tools.logging :as log]
            [trending-lisps.edn :as edn]
            [twitter.api.restful :as twitter]
            [twitter.oauth :as twitter-oauth]))

(def status-limit 140)
(def base-url "https://github.com")
(def cfg (edn/read-file "config.edn"))
(def twitter-creds
  (let [twitter-cfg (:twitter cfg)]
    (twitter-oauth/make-oauth-creds (:app-consumer-key twitter-cfg)
                                    (:app-consumer-secret twitter-cfg)
                                    (:user-access-token twitter-cfg)
                                    (:user-access-secret twitter-cfg))))

(defn get-short-name [name]
  (second (clojure.string/split name #"/")))

(defn build-status [lang name desc name-short]
  (let [link (str base-url "/" name)
        status (str name-short " - " desc " " link)]
    (if (> (count status) status-limit)
      (str "[" lang "] " name-short " " link)
      status)))

(defn twit-repo [lang name desc]
  (let [status (build-status lang name desc (get-short-name name))]
    (try
      (do
        (log/debug "twitting:" status)
        ;(twitter/statuses-update :oauth-creds twitter-creds
        ;                         :params {:status status})
        )
      (catch Exception e
        (log/error "error:" (.getMessage e))))))
