(defproject org.cyverse/cyverse-groups-client "0.1.0"
  :description "A Clojure client library for the CyVerse groups service."
  :url "https://github.com/cyverse-de/cyverse-groups-client"
  :license {:name "BSD"
            :url "http://cyverse.org/sites/default/files/iPLANT-LICENSE.txt"}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :plugins [[test2junit "1.2.2"]]
  :dependencies [[cheshire "5.7.0"]
                 [clj-http "3.4.1"]
                 [com.cemerick/url "0.1.1" :exclusions [com.cemerick/clojurescript.test]]
                 [medley "0.8.4"]
                 [org.clojure/clojure "1.8.0"]]
  :profiles {:test {:dependencies [[clj-http-fake "1.0.2"]]}})
