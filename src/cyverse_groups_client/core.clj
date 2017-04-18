(ns cyverse-groups-client.core
  (:use [medley.core :only [remove-vals]])
  (:require [cemerick.url :as curl]
            [clj-http.client :as http]
            [clojure.string :as string]))

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

  (find-groups [_ user search] [_ user search folder]
    "Searches for groups by name.")

  (add-group [_ user name type description]
    "Creates a new group.")

  (delete-group [_ user name]
    "Deletes an existing group.")

  (get-group [_ user name]
    "Retrieves information about a group.")

  (update-group [_ user name description]
    "Updates an existing group.")

  (list-group-privileges [_ user name]
    "Lists group privileges.")

  (revoke-group-privilege [_ user name subject privilege]
    "Revokes group privileges from a subject.")

  (grant-group-privilege [_ user name subject privilege]
    "Grants group privileges to a subject.")

  (list-group-members [_ user name]
    "Lists group members.")

  (replace-group-members [_ user name subjects]
    "Removes all existing members from a group and adds new members.")

  (remove-group-member [_ user name subject]
    "Removes a member from a group.")

  (add-group-member [_ user name subject]
    "Adds a member to a group.")

  (find-subjects [_ user search]
    "Finds subjects by name.")

  (get-subject [_ user subject]
    "Retrieves information about a subject.")

  (list-subject-groups [_ user subject]
    "Lists groups that a subject belongs to."))

(defn- build-url [base-url & path-elements]
  (str (apply curl/url base-url (mapv curl/url-encode path-elements))))

(defn- prepare-opts [opts ks]
  (remove-vals nil? (select-keys opts ks)))

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

  (revoke-folder-privilege [_ user name subject privilege]
    (:body (http/delete (build-url base-url "folders" name "privileges" subject privilege)
                        {:query-params {:user user}
                         :as           :json}))))

(defn new-cyverse-groups-client [base-url environment-name]
  (CyverseGroupsClient. base-url environment-name))
