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
               commit_message varchar(80),
               project_url varchar(80));

drop table changeset;
create table changeset(id int primary key auto_increment,
                          commit_id int,
                          add_line int,
                          delete_line int,
                          file varchar(80));
                          
drop table urtlog;
create table urtlog(id int primary key auto_increment,
               name varchar(20), 
               operate_date timestamp, 
               issue_no int,
               issue_title varchar(80),
               operator varchar(20), 
               operator_type varchar(10), 
               project_url varchar(80));

)


(defentity commit)
(defentity changeset)
(defentity urtlog)

; define commitlog record
(defrecord commitlog-rec 
  [author,commit_date,revision, commit_message,project_url ])

(defn insert-commitlog
  [rec ]
(insert commitlog 
        (values rec)))
;;; define changeset record
(defrecord changeset-rec
  [commit_id,add_line, delete_line, file])

(defn insert-changeset
  [rec]
  (insert changeset
          (values rec)))






                  
