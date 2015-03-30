(ns codestat.mysql)

;;;
;;; access to mysql database here
;;;
(use 'korma.db)
(defdb db (mysql {:db "codestat"
                  :user "codestat"
                  :password "codestat"}))
(use 'korma.core)

;;;
;;; define database record here
;;; gitlog [id name date revision commit_message change_line  project_url]
;;; svnlog [id name date revision log_message change_line project_url ]
;;; urtlog [id name date issue_no operator operate_type project_url]
;;;
;;; create table gitlog(id int primary key auto_increment,
;;;               name varchar(20), commit_date timestamp , revision varchar(40), 
;;;               commit_message varchar(80), change_line int, project_url varchar(80));
;;;
;;; create table svnlog(id int primary key auto_increment,
;;;               name varchar(20), commit_date timestamp, revision int,
;;;               log_message varchar(80), change_line int, project_url varchar(80));
;;;
;;; create table urtlog(id int primary key auto_increment,
;;;               name varchar(20), operate_date timestamp, issue_no int,
;;;               operator varchar(20), operator_type varchar(10), project_url varchar(80));
;;;
(defentity gitlog)
(defentity svnlog)
(defentity urtlog)





                  
