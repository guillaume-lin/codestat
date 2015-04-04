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
drop table gitlog;
create table gitlog(id int primary key auto_increment,
               name varchar(20), 
               commit_date timestamp , 
               revision varchar(40), 
               commit_message varchar(80), 
               add_line int , 
               delete_line int, 
               project_url varchar(80));

drop table svnlog;
create table svnlog(id int primary key auto_increment,
               name varchar(20), 
               commit_date timestamp, 
               revision int,
               log_message varchar(80), 
               add_line int, 
               delete_line int, 
               project_url varchar(80));

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


(defentity gitlog)
(defentity svnlog)
(defentity urtlog)

(defrecord gitlog_rec 
  [name,commit_date,revision, commit_message,
   add_line,delete_line,project_url ])
(defn insert-gitlog
  [rec ]
(insert gitlog 
        (values rec)))






                  
