(defproject org.cyverse/cyverse-groups-client "0.1.9"
  :description "A Clojure client library for the CyVerse groups service."
  :url "https://github.com/cyverse-de/cyverse-groups-client"
  :license {:name "BSD"
            :url  "https://cyverse.org/license"}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :plugins [[jonase/eastwood "1.4.3"]
            [lein-ancient "0.7.0"]
            [test2junit "1.4.4"]]
  :dependencies [[cheshire "5.13.0"]
                 [clj-http "3.13.0"]
                 [com.cemerick/url "0.1.1" :exclusions [com.cemerick/clojurescript.test]]
                 [medley "1.4.0"]
                 [org.clojure/clojure "1.11.3"]]
  :profiles {:dev  {:dependencies [[clj-http-fake "1.0.4"]]}
             :test {:dependencies [[clj-http-fake "1.0.4"]]}})
