(ns trending-lisps.cache
  (:require [trending-lisps.edn :as edn]
            [taoensso.carmine :as carmine :refer [wcar]]))

(def cache-expire (* 60 60 24 7 2)) ; cache lasts 2 weeks
(def cfg (edn/read-file "config.edn"))

(def conn {:pool {}
           :spec (:cache cfg)})

(defmacro wcar* [& body] `(carmine/wcar conn ~@body))

(defn get-cache [key]
  (wcar* (carmine/get key)))

(defn set-cache [key value]
  (wcar* (carmine/set key value)
         (carmine/expire key cache-expire)))
