(ns trending-lisps.twit
  (:require [trending-lisps.edn :as edn]
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

(defn build-status [name desc name-short]
  (let [link (str base-url "/" name)
        status (str name-short " - " desc " " link)]
    (if (> (count status) status-limit)
      (str name-short " " link)
      status)))

(defn twit-repo [name desc]
  (let [status (build-status name desc (get-short-name name))]
    (try
      (twitter/statuses-update :oauth-creds twitter-creds
                               :params {:status status})
      (catch Exception e (prn (.getMessage e))))))
