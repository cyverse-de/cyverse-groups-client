(ns cyverse-groups-client.core
  (:require [cemerick.url :as curl]
            [clj-http.client :as http]
            [clojure.string :as string]))

(defprotocol Client
  "A client library for the CyVerse Groups API."
  (get-status [_]
    "Retrieves information about the status of the CyVerse Groups service.")

  (find-folders [_ user search]
    "Searches for folders by name.")

  (add-folder [_ user name description]
    "Creates a new folder.")

  (delete-folder [_ user name]
    "Deletes an existing folder.")

  (get-folder [_ user name]
    "Retrieves information about a folder.")

  (update-folder [_ user name description]
    "Updates an existing folder.")

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
