(ns codestat.git)
(use 'clojure.java.shell)  ;;; to import sh

;;;==========================================
;;; data extraction and data translation
;;;==========================================

(def ^:dynamic *workspace* "/home/jenkins/workspace")

(defrecord gitlog-rec
  [])

;;;
(defn git-pull
  [dir]
  (with-sh-dir dir 
  (sh "git" "pull")))
;;; 
;;; return a line-seq of git logs
;;;
(defn git-log
  [dir]
  (line-seq (java.io.BufferedReader. 
              (java.io.StringReader. 
                (:out (with-sh-dir dir
                        (sh "git" "log" "--shortstat"))))))
;;;
(defn get-branches
  [project-url]
  ())

;;;=============================================
;;; data manipulation
;;;=============================================
;;;
;;; return a lazy-seq of git log records
;;;
(defn get-gitlog-seq
  [branch]
  ())

(defn insert-git-log
  []
  ())

(defn count-by-author-change-line
  ())
