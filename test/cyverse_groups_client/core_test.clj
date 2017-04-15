(ns cyverse-groups-client.core-test
  (:require [cemerick.url :as curl]
            [cheshire.core :as json]
            [clj-http.fake :refer :all]
            [clojure.string :as string]
            [clojure.test :refer :all]
            [cyverse-groups-client.core :as c]))

(defn- success-fn
  ([]
   (constantly {:status 200 :body ""}))
  ([body]
   (constantly {:status 200 :headers {"Content-Type" "application/json"} :body (json/encode body)})))

(def fake-base-url "http://groups.example.org/")
(def fake-env "fake-env")
(def fake-user "ekaf")
(def search-term "e")

(defn- fake-url [& components]
  (str (apply curl/url fake-base-url components)))

(defn- fake-query-url [query & components]
  (str (assoc (apply curl/url fake-base-url components) :query query)))

(defn- create-fake-client []
  (c/new-cyverse-groups-client fake-base-url fake-env))

(def fake-status
  {:service     "iplant-groups"
   :description "RESTful facade for the Grouper API."
   :version     "4.2.42"
   :docs-url    (fake-url "docs")
   :expecting   ""
   :grouper     true})

(deftest test-get-status
  (with-fake-routes {fake-base-url {:get (success-fn fake-status)}}
    (is (= (c/get-status (create-fake-client))
           fake-status))))

(def fake-folders
  {:folders
   [{:name              "baz:bar:foo"
     :description       "Foo"
     :display_extension "bar"
     :display_name      "baz"
     :extension         "foo"
     :id_index          "27"
     :id                "72"}]})

(deftest test-find-folders
  (let [query {:user fake-user :search search-term}]
    (with-fake-routes {(fake-query-url query "folders") {:get (success-fn fake-folders)}}
      (is (= (c/find-folders (create-fake-client) fake-user search-term)
             fake-folders)))))
