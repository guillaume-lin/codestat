(ns codestat.mysql)

;;;
;;; access to mysql database here
;;;
(use 'korma.db)
(defdb db (mysql {:db "codestat"
                  :user "codestat"
                  :password "codestat"}))
(use 'korma.core)

(comment 
drop table project;
create table project(id int primary key auto_increment,
                     project_name varchar(255),
                     project_desc varchar(255),
                     vcs_url varchar(255) unique,
                     vcs_login varchar(20),
                     vcs_pass varchar(20),
                     issue_url varchar(255) unique,
                     issue_login varchar(20),
                     issue_pass varchar(40));
drop table author;
create table author (id int primary key auto_increment,
                     author_name varchar(40) unique,
                     author_grade int);
  
drop table commit;
create table commit(id int primary key auto_increment,
               author_name varchar(40), 
               commit_date timestamp , 
               revision varchar(40) unique, 
               message varchar(1024),
               branch varchar(255),
               vcs_url varchar(255),
               ignore_commit bool);

drop table changeset;
create table changeset(id int primary key auto_increment,
                          commit_id int,
                          add_line int,
                          delete_line int,
                          file varchar(1024));
drop table issue;
create table issue (issue_no int primary key,
                    issue_title varchar(255),
                    issue_status varchar(20),
                    issue_assignee varchar(20));

drop table issuelog;
create table issuelog(id int primary key auto_increment,
               issue_no int,
               operator varchar(40), 
               operate_date timestamp, 
               operator_type varchar(10),
               assignee varchar(40),
               project_id int);

)

(defrecord commit-rec
  [author_name commit_date revision message])
(defrecord change-rec
  [add_line delete_line file])
(defrecord log-rec
  [commit-rec changeset-rec])


(defentity project)
(defentity author)
(defentity commit)
(defentity changeset)
(defentity issue)
(defentity issuelog)


(defn insert-commit
  [rec vcs_url branch]
  ;(println "insert commit: " rec)
  (insert commit 
          (values {:author_name (:author_name rec)
                   :commit_date (java.sql.Timestamp. (:commit_date rec))
                   :revision (:revision rec)
                   :message (:message rec)
                   :vcs_url vcs_url
                   :branch branch
                   })))

(defn insert-changeset
  [commit_id rec]
  ;(println "insert changeset: " commit_id rec)
  (insert changeset
          (values {:commit_id commit_id
                   :add_line (:add_line rec)
                   :delete_line (:delete_line rec)
                   :file (:file rec)})))

(defn insert-log
  [rec vcs_url branch]
  (try 
    (if-let [commit_id (:generated_key (insert-commit (:commit-rec rec) vcs_url branch))]
      (doseq [r (:changeset-rec rec)]
        (insert-changeset commit_id r)))
    (catch Exception e
      (println ">>> insert-log Exception:" (.getMessage e)))))

;;;
;;; insert a project
;;;
(defn insert-project
  [rec]
  (insert project
          (values (dissoc rec :project_id)))); // remove project_id in the record
;;;
;;; query all the project
;;;
(defn query-project
  []
  (select project))

(defn query-project-by-vcs-url
  [vcs-url]
  (select project (where {:vcs_url vcs-url})))

(defn query-commit-by-project-id
  [project-id]
  (select commit (where {:project_id project-id})))

(defn query-changeset-by-commit-id
  [commit-id]
  (select changeset (where {:commit_id commit-id})))

(defn query-commit
  []
  (select commit))

(defn query-commit-by-author
  [author-id]
  (select commit (where {:author_id author-id})))

(defn query-author-id-by-author-name
  [author-name]
  (select author (where {:author_name author-name})))

(defn query-author-by-commit-id
  [commit-id]
  (select commit 
          (fields [:author_name])
          (where {:id commit-id})))
(defn query-changeset
  []
  (select changeset))


(defn update-commit-mark-ignore
  "mark the specified commit to be ignored"
  [id, bool]
  (update commit
          (set-fields {:ignore_commit bool})
          (where {:id [= id]})))






                  
