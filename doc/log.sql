#
# use mysql source command to load this file.
# source doc/log.sql
#
drop table gitlog;
create table gitlog(id int primary key auto_increment,
               name varchar(20), commit_date timestamp , revision varchar(40), 
               commit_message varchar(80), add_line int , delete_line int, project_url varchar(80));

drop table svnlog;
create table svnlog(id int primary key auto_increment,
               name varchar(20), commit_date timestamp, revision int,
               log_message varchar(80), add_line int, delete_line int, project_url varchar(80));

drop table urtlog;
create table urtlog(id int primary key auto_increment,
               name varchar(20), operate_date timestamp, issue_no int,issue_title varchar(80),
               operator varchar(20), operator_type varchar(10), project_url varchar(80));
