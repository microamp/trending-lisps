(ns trending-lisps.cache
  (:require [clojure.tools.logging :as log]
            [taoensso.carmine :as carmine :refer [wcar]]
            [environ.core :refer [env]]))

(def cache-spec {:host (env :cache-host)
                 :port (read-string (env :cache-port))
                 :password (env :cache-password)})
(def conn {:pool {}
           :spec cache-spec})

(defmacro wcar* [& body] `(carmine/wcar conn ~@body))

(defn get-cache [key]
  (wcar* (carmine/get key)))

(defn set-cache [key value expire]
  (wcar* (carmine/set key value)
         (carmine/expire key expire)))
