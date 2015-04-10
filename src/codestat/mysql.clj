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
drop table commit;
create table commit(id int primary key auto_increment,
               author varchar(20), 
               commit_date timestamp , 
               revision varchar(40), 
               message varchar(255),
               project_url varchar(255));

drop table changeset;
create table changeset(id int primary key auto_increment,
                          commit_id int,
                          add_line int,
                          delete_line int,
                          file varchar(255));
                          
drop table urtlog;
create table urtlog(id int primary key auto_increment,
               name varchar(20), 
               operate_date timestamp, 
               issue_no int,
               issue_title varchar(80),
               operator varchar(20), 
               operator_type varchar(10), 
               project_url varchar(255));

)


(defentity commit)
(defentity changeset)
(defentity urtlog)


(defn insert-commit
  [rec ]
(insert commit 
        (values {:author (:author rec)
                 :commit_date (java.sql.Timestamp. (* 1000 (Integer/parseInt (:commit_date rec))))
                 :revision (:revision rec)
                 :message (:message rec)
                 })))

(defn insert-changeset
  [commit_id rec ]
  (insert changeset
          (values {:commit_id commit_id
                   :add_line (:add_line rec)
                   :delete_line (:delete_line rec)
                   :file (:file rec)})))

(defn insert-log
  [rec ]
  (if-let [commit_id (:generated_key (insert-commit (:commit-rec rec)))]
    (for [r (:changeset-rec rec)]
      (insert-changeset commit_id r))))









                  
