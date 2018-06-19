# iplant-groups-client

A client library for the CyVerse groups service.

## Usage

``` clojure
(require '[cyverse-groups-client.core :as c])

;; Create a client instance.
(def client = (c/new-cyverse-groups-client base-url environment-name))

;; Get information about the status of the service.
(c/get-status client)

;; Format the folder name prefix for the current environment.
(c/get-folder-name-prefix client)

;; Add the folder name prefix to a partial foldername.
(c/build-folder-name client "some:folder:name")

;; Find folders.
(c/find-folders client "username" "search-string")

;; Add a folder with the default display extension (the last component of the folder name).
(c/add-folder client "username" "some:folder:name" "Description")

;; Add a folder with a custom display extension.
(c/add-folder client "username" "some:folder:name" "Description" "custom-extension")

;; Delete a folder.
(c/delete-folder client "username" "some:folder:name")

;; Get information about a specific folder.
(c/get-folder client "username" "some:folder:name")

;; Update an existing folder.
(c/update-folder client "username" "some:folder:name" {:name              "new:folder:name"
                                                       :description       "New folder description"
                                                       :display_extension "new-extension"})

;; List folder privileges.
(c/list-folder-privileges client "username" "some:folder:name")

;; Revoke folder privileges.
(c/revoke-folder-privilege client "username" "some:folder:name" "subject" "privilege")

;; Grant folder privileges.
(c/grant-folder-privilege client "username" "some:folder:name" "subject" "privilege")

;; Find groups in any folder.
(c/find-groups client "username" "search-string")

;; Find groups within a folder.
(c/find-groups client "username" "search-string" "some:folder:name")

;; Add a group.
(c/add-group client "username" "some:group:name" "group" "Description.")
(c/add-group client "username" "some:role:name" "role" "Description.")

;; Delete a group.
(c/delete-group client "username" "some:group:name")

;; Get information about a group.
(c/get-group client "username" "some:group:name")

;; Update a group.
(c/update-group client "username" "some:group:name" {:name              "new:group:name"
                                                     :description       "New group description"
                                                     :display_extension "new-extension"})

;; List group privileges.
(c/list-group-privileges client "username" "some:group:name")
(c/list-group-privileges client "username" "some:group:name"
                         {:privilege         "privilege-name"
                          :subject-id        "subject-id"
                          :subject-source-id "subject-source-id"
                          :inheritance-level "immediate"})

;; Update group privileges for several users.
(c/update-group-privileges client "username" "some:group:name" {:updates [{:subject_id "subject"
                                                                           :privileges ["optin"]}
                                                                          {:subject_id "subject1"
                                                                           :privileges ["read" "optin"]}]})

;; Update group privileges without overwriting existing privileges.
(c/update-group-privileges client "username" "some:group:name" {:updates [{:subject_id "subject"
                                                                           :privileges ["optin"]}
                                                                          {:subject_id "subject1"
                                                                           :privileges ["read" "optin"]}]}
                           {:replace false})

;; Revoke group privileges for several users.
(c/revoke-group-privileges client "username" "some:group:name" {:updates [{:subject_id "subject"
                                                                           :privileges ["optin"]}
                                                                          {:subject_id "subject1"
                                                                           :privileges ["read" "optin"]}]})

;; Revoke group privileges.
(c/revoke-group-privilege client "username" "some:group:name" "subject" "privilege")

;; Grant group privileges.
(c/grant-group-privielge client "username" "some:group:name" "subject" "privilege")

;; List the members of a group.
(c/list-group-members client "username" "some:group:name")

;; Replace all members of a group.
(c/replace-group-members client "username" "some:group:name" ["subject1", "subject2"])

;; Remove a member from a group.
(c/remove-group-member client "username" "some:group:name" "subject")

;; Add a member to a group.
(c/add-group-member client "username" "some:group:name" "subject")

;; Find subjects.
(c/find-subjects client "username" "search-string")

;; Look up subjects.
(c/lookup-subjects client "username" ["subject1", "subject2"])

;; Get information about a specific subject.
(c/get-subject client "username" "subject")
```

## License

http://www.cyverse.org/sites/default/files/CyVerse-LICENSE.txt
