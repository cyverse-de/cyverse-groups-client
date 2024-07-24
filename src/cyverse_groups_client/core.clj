(ns cyverse-groups-client.core
  (:use [medley.core :only [remove-vals]])
  (:require [cemerick.url :as curl]
            [clj-http.client :as http]
            [clojure.string :as string]))

(def public-user "GrouperAll")

(defprotocol Client
  "A client library for the CyVerse Groups API."
  (get-status [_]
    "Retrieves information about the status of the CyVerse Groups service.")

  (find-folders [_ user search]
    "Searches for folders by name.")

  (add-folder [_ user name description] [_ user name description display-extension]
    "Creates a new folder.")

  (delete-folder [_ user name]
    "Deletes an existing folder.")

  (get-folder [_ user name]
    "Retrieves information about a folder.")

  (update-folder [_ user name updates]
    "Updates an existing folder. To make calls to this function as clean as possible, the updates are passed in as
     a map containing three possible elements: `:name`, `:description`, `:display_extension`.")

  (list-folder-privileges [_ user name]
    "Lists folder privileges.")

  (revoke-folder-privilege [_ user name subject privilege]
    "Revokes folder privileges from a subject.")

  (grant-folder-privilege [_ user name subject privilege]
    "Grants folder privileges to a subject.")

  (find-groups [_ user details search ] [_ user details search folder]
    "Searches for groups by name.")

  (add-group [_ user name type description]
    "Creates a new group.")

  (delete-group [_ user name]
    "Deletes an existing group.")

  (get-group [_ user name]
    "Retrieves information about a group.")

  (update-group [_ user name updates]
    "Updates an existing group.")

  (list-group-privileges [_ user name] [_ user name params]
    "Lists group privileges.")

  (update-group-privileges [_ user name updates] [_ user name updates params]
    "Sets group privileges for multiple subjects. The updates argument is in the same format as the request body
     for the endpoint. Please see the README.md file for more details.")

  (revoke-group-privileges [_ user name updates]
    "Revokes group privileges for multiple subjects. The updates argument is in the same format as the request body
     for the endpoint. Please see the README.md file for more details.")

  (revoke-group-privilege [_ user name subject privilege]
    "Revokes group privileges from a subject.")

  (grant-group-privilege [_ user name subject privilege]
    "Grants group privileges to a subject.")

  (list-group-members-by-id [_ user group-id] [_ user group-id params]
    "Lists the members of the group with the given identifier.")

  (list-group-members [_ user name] [_ user name params]
    "Lists group members.")

  (replace-group-members [_ user name subjects]
    "Removes all existing members from a group and adds new members.")

  (add-group-members [_ user name subjects]
    "Adds multiple members to a group.")

  (remove-group-members [_ user name subjects]
    "Removes multiple members from a group.")

  (remove-group-member [_ user name subject]
    "Removes a member from a group.")

  (add-group-member [_ user name subject]
    "Adds a member to a group.")

  (find-subjects [_ user search]
    "Finds subjects by name.")

  (lookup-subjects [_ user subjects]
    "Looks up one or more subjects by ID.")

  (get-subject [_ user subject]
    "Retrieves information about a subject.")

  (list-subject-groups [_ user subject details] [_ user subject details params]
    "Lists groups that a subject belongs to.")

  (list-subject-privileges [_ user subject] [_ user subject params]
    "Lists privileges that have been granted to a subject.")

  (get-folder-name-prefix [_]
    "Returns the folder name prefix for the environment.")

  (build-folder-name [_ partial-name]
    "Adds the folder name prefix to the partial folder name given in the argument list."))

(def ^:private valid-folder-privileges
  {:stem           "Allows users to create subfolders."
   :create         "Allows users to create a group in a folder."
   :stemAttrUpdate "Allows users to assign attributes to a folder."
   :stemAttrRead   "Allows users to read a folder's attributes."})

(defn- invalid-privilege-msg [type valid-privileges privilege]
  (->> (concat [(str "Invalid " type " privilege: " (name privilege) "\n")
                (str "Valid " type " privileges:")]
               (mapv (fn [[k v]] (str "\t" (name k) " - " v)) valid-privileges))
       (string/join "\n")))

(defn- validate-privilege [type valid-privileges privilege]
  (when-not (contains? valid-privileges (keyword privilege))
    (throw (IllegalArgumentException. ^String (invalid-privilege-msg type valid-privileges privilege)))))

(def ^:private validate-folder-privilege (partial validate-privilege "folder" valid-folder-privileges))

(defn- build-url [base-url & path-elements]
  (let [preprocess-path-element (fn [e] (if (keyword? e) (name e) e))]
    (str (apply curl/url base-url (mapv (comp curl/url-encode preprocess-path-element) path-elements)))))

(defn- folder-name-prefix [environment-name]
  (format "iplant:de:%s" environment-name))

(deftype CyverseGroupsClient [base-url environment-name]
  Client

  (get-status [_]
    (:body (http/get base-url {:as :json})))

  (find-folders [_ user search]
    (:body (http/get (build-url base-url "folders")
                     {:query-params {:user user :search search}
                      :as           :json})))

  (add-folder [self user name description]
    (add-folder self user name description nil))

  (add-folder [_ user name description display-extension]
    (:body (http/post (build-url base-url "folders")
                      {:query-params {:user user}
                       :form-params  (remove-vals nil? {:name              name
                                                        :description       description
                                                        :display_extension display-extension})
                       :content-type :json
                       :as           :json})))

  (delete-folder [_ user name]
    (:body (http/delete (build-url base-url "folders" name)
                        {:query-params {:user user}
                         :as           :json})))

  (get-folder [_ user name]
    (:body (http/get (build-url base-url "folders" name)
                     {:query-params {:user user}
                      :as           :json})))

  (update-folder [_ user name updates]
    (:body (http/put (build-url base-url "folders" name)
                     {:query-params {:user user}
                      :form-params  (remove-vals nil? (select-keys updates [:name :description :display_extension]))
                      :content-type :json
                      :as           :json})))

  (list-folder-privileges [_ user name]
    (:body (http/get (build-url base-url "folders" name "privileges")
                     {:query-params {:user user}
                      :as           :json})))

  (revoke-folder-privilege [_ user folder-name subject privilege]
    (validate-folder-privilege privilege)
    (:body (http/delete (build-url base-url "folders" folder-name "privileges" subject privilege)
                        {:query-params {:user user}
                         :as           :json})))

  (grant-folder-privilege [_ user folder-name subject privilege]
    (validate-folder-privilege privilege)
    (:body (http/put (build-url base-url "folders" folder-name "privileges" subject privilege)
                     {:query-params {:user user}
                      :as           :json})))

  (find-groups [self user details search]
    (find-groups self user details search nil))

  (find-groups [_ user details search folder]
    (:body (http/get (build-url base-url "groups")
                     {:query-params (remove-vals nil? {:user   user
                                                       :search search
                                                       :details details
                                                       :folder folder})
                      :as           :json})))

  (add-group [_ user name type description]
    (:body (http/post (build-url base-url "groups")
                      {:query-params {:user user}
                       :form-params  (remove-vals nil? {:name        name
                                                        :type        type
                                                        :description description})
                       :content-type :json
                       :as           :json})))

  (delete-group [_ user name]
    (:body (http/delete (build-url base-url "groups" name)
                        {:query-params {:user user}
                         :as           :json})))

  (get-group [_ user name]
    (:body (http/get (build-url base-url "groups" name)
                     {:query-params {:user user}
                      :as           :json})))

  (update-group [_ user name updates]
    (:body (http/put (build-url base-url "groups" name)
                     {:query-params {:user user}
                      :form-params  (remove-vals nil? (select-keys updates [:name :description :display_extension]))
                      :content-type :json
                      :as           :json})))

  (list-group-privileges [self user name]
    (list-group-privileges self user name {}))

  (list-group-privileges [_ user name params]
    (let [accepted-params [:privilege :subject-id :subject-source-id :inheritance-level]]
      (:body (http/get (build-url base-url "groups" name "privileges")
                       {:query-params (merge {:user user} (select-keys params accepted-params))
                        :as           :json}))))

  (update-group-privileges [this user name updates]
    (update-group-privileges this user name updates {}))

  (update-group-privileges [_ user name updates params]
    (:body (http/post (build-url base-url "groups" name "privileges")
                      {:query-params (assoc params :user user)
                       :form-params  updates
                       :content-type :json
                       :as           :json})))

  (revoke-group-privileges [_ user name updates]
    (:body (http/post (build-url base-url "groups" name "privileges" "deleter")
                      {:query-params {:user user}
                       :form-params  updates
                       :content-type :json
                       :as           :json})))

  (revoke-group-privilege [_ user name subject privilege]
    (:body (http/delete (build-url base-url "groups" name "privileges" subject privilege)
                        {:query-params {:user user}
                         :as           :json})))

  (grant-group-privilege [_ user name subject privilege]
    (:body (http/put (build-url base-url "groups" name "privileges" subject privilege)
                     {:query-params {:user user}
                      :as           :json})))

  (list-group-members-by-id [self user group-id]
    (list-group-members-by-id self user group-id {}))

  (list-group-members-by-id [_ user group-id params]
    (let [accepted-params [:member-filter]]
      (:body (http/get (build-url base-url "groups" "id" group-id "members")
                       {:query-params (merge {:user user}
                                             (remove-vals nil? (select-keys params accepted-params)))
                        :as           :json}))))

  (list-group-members [self user name]
    (list-group-members self user name {}))

  (list-group-members [_ user name params]
    (let [accepted-params [:member-filter]]
      (:body (http/get (build-url base-url "groups" name "members")
                       {:query-params (merge {:user user}
                                             (remove-vals nil? (select-keys params accepted-params)))
                        :as           :json}))))

  (replace-group-members [_ user name subjects]
    (:body (http/put (build-url base-url "groups" name "members")
                     {:query-params {:user user}
                      :form-params  {:members subjects}
                      :content-type :json
                      :as           :json})))

  (add-group-members [_ user name subjects]
    (:body (http/post (build-url base-url "groups" name "members")
                      {:query-params {:user user}
                       :form-params  {:members subjects}
                       :content-type :json
                       :as           :json})))

  (remove-group-members [_ user name subjects]
    (:body (http/post (build-url base-url "groups" name "members" "deleter")
                      {:query-params {:user user}
                       :form-params  {:members subjects}
                       :content-type :json
                       :as           :json})))

  (remove-group-member [_ user name subject]
    (:body (http/delete (build-url base-url "groups" name "members" subject)
                        {:query-params {:user user}
                         :as           :json})))

  (add-group-member [_ user name subject]
    (:body (http/put (build-url base-url "groups" name "members" subject)
                     {:query-params {:user user}
                      :as           :json})))

  (find-subjects [_ user search]
    (:body (http/get (build-url base-url "subjects")
                     {:query-params {:user user :search search}
                      :as           :json})))

  (lookup-subjects [_ user subjects]
    (:body (http/post (build-url base-url "subjects" "lookup")
                      {:query-params {:user user}
                       :form-params  {:subject_ids subjects}
                       :content-type :json
                       :as           :json})))

  (get-subject [_ user subject]
    (:body (http/get (build-url base-url "subjects" subject)
                     {:query-params {:user user}
                      :as           :json})))

  (list-subject-groups [_ user subject details]
    (:body (http/get (build-url base-url "subjects" subject "groups")
                     {:query-params (remove-vals nil? {:user user :details details})
                      :as           :json})))

  (list-subject-groups [_ user subject details folder]
    (:body (http/get (build-url base-url "subjects" subject "groups")
                     {:query-params (remove-vals nil? {:user user :details details :folder folder})
                      :as           :json})))

  (list-subject-privileges [self user subject]
    (list-subject-privileges self user subject {}))

  (list-subject-privileges [self user subject params]
    (let [accepted-params [:inheritance-level :entity-type :folder]]
      (:body (http/get (build-url base-url "subjects" subject "privileges")
                       {:query-params (merge {:user user}
                                             (remove-vals nil? (select-keys params accepted-params)))
                        :as           :json}))))

  (get-folder-name-prefix [_]
    (folder-name-prefix environment-name))

  (build-folder-name [_ partial-name]
    (format "%s:%s" (folder-name-prefix environment-name) partial-name)))

(defn new-cyverse-groups-client [base-url environment-name]
  (CyverseGroupsClient. base-url environment-name))
