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
(def fake-folder "foo:bar")

(defn- fake-url [& components]
  (str (apply curl/url fake-base-url (mapv curl/url-encode components))))

(defn- fake-query-url [query & components]
  (str (assoc (apply curl/url fake-base-url (mapv curl/url-encode components)) :query query)))

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

(defn- create-fake-folder [{name :name description :description display-extension :display_extension}]
  (remove-vals nil? {:name              name
                     :description       description
                     :display_extension display-extension
                     :extension         (last (string/split name #":"))
                     :id_index          "42"
                     :id                "84"}))

(defn- add-folder-response [request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/encode (create-fake-folder (json/decode (slurp (:body request)) true)))})

(deftest test-add-folder
  (with-fake-routes {(fake-query-url {:user fake-user} "folders") {:post add-folder-response}}
    (is (= (c/add-folder (create-fake-client) fake-user "foo:bar:baz" "A random description")
           (create-fake-folder {:name "foo:bar:baz" :description "A random description"})))
    (is (= (c/add-folder (create-fake-client) fake-user "bar:baz:quux" "desc" "disp")
           (create-fake-folder {:name "bar:baz:quux" :description "desc" :display_extension "disp"})))))

(defn- folder-response [{:keys [uri]}]
  (let [name (curl/url-decode (last (string/split uri #"/")))]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/encode (create-fake-folder {:name name :description ""}))}))

(deftest test-delete-folder
  (with-fake-routes {(fake-query-url {:user fake-user} "folders" "baz:quux:blrfl") {:delete folder-response}}
    (is (= (c/delete-folder (create-fake-client) fake-user "baz:quux:blrfl")
           (create-fake-folder {:name "baz:quux:blrfl" :description ""})))))

(deftest test-get-folder
  (with-fake-routes {(fake-query-url {:user fake-user} "folders" "quux:blrfl:blah") {:get folder-response}}
    (is (= (c/get-folder (create-fake-client) fake-user "quux:blrfl:blah")
           (create-fake-folder {:name "quux:blrfl:blah" :description ""})))))

(defn- update-folder-response [{:keys [uri body]}]
  (let [name     (curl/url-decode (last (string/split uri #"/")))
        original (create-fake-folder {:name name :description ""})
        updates  (json/decode (slurp body) true)
        updated  (create-fake-folder (select-keys (merge original updates) [:name :description :display_extension]))]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/encode updated)}))

(deftest test-update-folder
  (with-fake-routes {(fake-query-url {:user fake-user} "folders" "a:b:c") {:put update-folder-response}}
    (let [client (create-fake-client)]
      (is (= (c/update-folder client fake-user "a:b:c" {:name              "d:e:f"
                                                        :description       "desc"
                                                        :display_extension "ext"})
             (create-fake-folder {:name              "d:e:f"
                                  :description       "desc"
                                  :display_extension "ext"})))
      (is (= (c/update-folder client fake-user "a:b:c" {:name "d:e:f"})
             (create-fake-folder {:name "d:e:f" :description ""})))
      (is (= (c/update-folder client fake-user "a:b:c" {:description "foo"})
             (create-fake-folder {:name "a:b:c" :description "foo"})))
      (is (= (c/update-folder client fake-user "a:b:c" {:display_extension "bar"})
             (create-fake-folder {:name "a:b:c" :description "" :display_extension "bar"}))))))

(def ^:private fake-privilege
  {:type "naming"
   :name "stem"
   :allowed true
   :revokable true
   :subject {:id fake-user
             :name "Resu Ekaf"
             :first_name "Resu"
             :last_name "Ekaf"
             :email "ekaf@example.org"
             :institution ""
             :source_id "ldap"}})

(defn- privilege-response [_]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/encode fake-privilege)})

(defn- fake-folder-privilege [folder-fields]
  {:privileges [(assoc fake-privilege :folder (create-fake-folder folder-fields))]})

(defn- folder-privileges-response [{:keys [uri]}]
  (let [name (curl/url-decode (last (butlast (string/split uri #"/"))))]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/encode (fake-folder-privilege {:name name :description ""}))}))

(deftest test-folder-privilege-listing
  (with-fake-routes {(fake-query-url {:user fake-user} "folders" "a:b:c" "privileges")
                     {:get folder-privileges-response}}
    (is (= (c/list-folder-privileges (create-fake-client) fake-user "a:b:c")
           (fake-folder-privilege {:name "a:b:c" :description ""})))))

(deftest test-folder-privilege-revocation
  (with-fake-routes {(fake-query-url {:user fake-user} "folders" "a:b:c" "privileges" "nobody" "naming")
                     {:delete privilege-response}}
    (is (= (c/revoke-folder-privilege (create-fake-client) fake-user "a:b:c" "nobody" "naming")
           fake-privilege))))

(deftest test-folder-privilege-granting
  (with-fake-routes {(fake-query-url {:user fake-user} "folders" "a:b:c" "privileges" "nobody" "naming")
                     {:put privilege-response}}
    (is (= (c/grant-folder-privilege (create-fake-client) fake-user "a:b:c" "nobody" "naming")
           fake-privilege))))

(def fake-groups
  {:groups [{:name              "foo:bar:baz:quux"
             :type              "role"
             :display_extension "quux"
             :display_name      "foo:bar:baz:quux"
             :extension         "quux"
             :id_index          "24"
             :id                "42"}
            {:name              "foo:bar:baz:blargh"
             :type              "role"
             :display_extension "blargh"
             :display_name      "foo:bar:baz:blargh"
             :extension         "blargh"
             :id_index          "27"
             :id                "72"}]})

(def fake-group (get-in fake-groups [:groups 0]))

(deftest test-find-groups
  (let [query {:user fake-user :search search-term}]
    (with-fake-routes {(fake-query-url query "groups") {:get (success-fn fake-groups)}}
      (is (= (c/find-groups (create-fake-client) fake-user search-term)
             fake-groups))))
  (let [query {:user fake-user :search search-term :folder fake-folder}]
    (with-fake-routes {(fake-query-url query "groups") {:get (success-fn fake-groups)}}
      (is (= (c/find-groups (create-fake-client) fake-user search-term fake-folder)
             fake-groups)))))

(defn- create-fake-group [{:keys [name type description]}]
  (remove-vals nil? {:name        name
                     :type        type
                     :description description
                     :id          "2112"
                     :id_index    "1812"}))

(defn- add-group-response [request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/encode (create-fake-group (json/decode (slurp (:body request)) true)))})

(deftest test-add-group
  (with-fake-routes {(fake-query-url {:user fake-user} "groups") {:post add-group-response}}
    (is (= (c/add-group (create-fake-client) fake-user "foo:bar:baz" "role" "Some description.")
           (create-fake-group {:name        "foo:bar:baz"
                               :type        "role"
                               :description "Some description."})))))

(deftest test-delete-group
  (let [group fake-group]
    (with-fake-routes {(fake-query-url {:user fake-user} "groups" (:name group)) {:delete (success-fn group)}}
      (is (= (c/delete-group (create-fake-client) fake-user (:name group))
             group)))))

(deftest test-get-group
  (let [group fake-group]
    (with-fake-routes {(fake-query-url {:user fake-user} "groups" (:name group)) {:get (success-fn group)}}
      (is (= (c/get-group (create-fake-client) fake-user (:name group))
             group)))))

(defn- group-update-response [orig request]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/encode (merge orig (json/decode (slurp (:body request)) true)))})

(defn- run-group-update-test [orig updates]
  (is (= (c/update-group (create-fake-client) fake-user (:name orig) updates)
         (merge orig updates))))

(deftest test-update-group
  (let [group fake-group]
    (with-fake-routes {(fake-query-url {:user fake-user} "groups" (:name group))
                       {:put (partial group-update-response group)}}
      (run-group-update-test group {:name "bar:baz:quux:blrfl"})
      (run-group-update-test group {:display_extension "foo"})
      (run-group-update-test group {:description "This is a somewhat unusual description."}))))

(def fake-group-privilege (assoc fake-privilege :group fake-group))

(def fake-group-privileges {:privileges [fake-group-privilege]})

(deftest test-group-privilege-listing
  (with-fake-routes {(fake-query-url {:user fake-user} "groups" (:name fake-group) "privileges")
                     {:get (success-fn fake-group-privileges)}}
    (is (= (c/list-group-privileges (create-fake-client) fake-user (:name fake-group))
           fake-group-privileges))))

(deftest test-group-privilege-revocation
  (with-fake-routes {(fake-query-url {:user fake-user} "groups" "a:b:c" "privileges" fake-user "read")
                     {:delete (success-fn fake-privilege)}}
    (is (= (c/revoke-group-privilege (create-fake-client) fake-user "a:b:c" fake-user "read")
           fake-privilege))))
