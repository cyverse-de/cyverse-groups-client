(ns cyverse-groups-client.core-test
  (:require [cemerick.url :as curl]
            [cheshire.core :as json]
            [clj-http.fake :refer :all]
            [clojure.string :as string]
            [clojure.test :refer :all]
            [cyverse-groups-client.core :as c]
            [medley.core :refer [remove-vals]]))

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

(defn create-fake-folder [{name :name description :description display-extension :display_extension}]
  (remove-vals nil? {:name              name
                     :description       description
                     :display_extension display-extension
                     :extension         (last (string/split name #":"))
                     :id_index          "42"
                     :id                "84"}))

(defn add-folder-response [request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/encode (create-fake-folder (json/decode (slurp (:body request)) true)))})

(deftest test-add-folder
  (with-fake-routes {(fake-query-url {:user fake-user} "folders") {:post add-folder-response}}
    (is (= (c/add-folder (create-fake-client) fake-user "foo:bar:baz" "A random description")
           (create-fake-folder {:name "foo:bar:baz" :description "A random description"})))
    (is (= (c/add-folder (create-fake-client) fake-user "bar:baz:quux" "desc" "disp")
           (create-fake-folder {:name "bar:baz:quux" :description "desc" :display_extension "disp"})))))

(defn delete-folder-response [{:keys [query-string]}]
  (let [query-params (curl/query->map query-string)]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/encode (create-fake-folder {:name (get query-params "folder-name") :description ""}))}))

(deftest test-delete-folder
  (with-fake-routes {(fake-query-url {:user fake-user :folder-name "baz:quux:blrfl"} "folders")
                     {:delete delete-folder-response}}
    (is (= (c/delete-folder (create-fake-client) fake-user "baz:quux:blrfl")
           (create-fake-folder {:name "baz:quux:blrfl" :description ""})))))
