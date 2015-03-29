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
;;; commit-log [id user date log-message revision project-url ]
;;; change-log [id user date change-line revision project-url ]
;;; urtracker-log [id user date issue-no project-url] 
;;;
(defentity 
(defentity users)
(select users)

                  
