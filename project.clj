(defproject trending-lisps "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.12"]
                 [log4j/log4j "1.2.17"]
                 [enlive "1.1.5"]
                 [overtone/at-at "1.2.0"]
                 [com.taoensso/carmine "2.9.2"]
                 [twitter-api "0.7.8"]]
  :plugins [[lein-environ "1.0.0"]]
  :main trending-lisps.core)
