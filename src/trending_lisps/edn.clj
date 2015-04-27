(ns trending-lisps.edn
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn read-file [filename]
  (-> filename
      io/resource
      slurp
      edn/read-string))
