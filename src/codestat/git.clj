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
(defn git-log-1
  [dir]
  (line-seq (java.io.BufferedReader. 
              (java.io.StringReader. 
                (:out (with-sh-dir dir
                        (sh "git" "log" "--numstat" "--format='Author:%an%nDate:%at%nCommit:%H%nMessage:%s'")))))))
;;;
(defn get-branches
  [project-url]
  ())

;;; 
;;; log format as:
;;; git log --numstat --format="Author:%an%nDate:%at%nCommit:%H%nMessage:%s"
;;;
(comment
Author:lx.mao
Date:1428461520
Commit:85fa48a1409bb22b70b9d4d6173766d7777e741d
Message:[TvApp]Show channel logo and current program's thumbnail on channel list.

6       0       source/TvApp/res/layout/channel_item.xml
4       2       source/TvApp/res/values/dimens.xml
8       3       source/TvApp/src/org/droidtv/playtvapp/browse/ChannelAdapter.java
20      16      source/TvApp/src/org/droidtv/playtvapp/browse/ChannelItemView.java
0       7       source/TvApp/src/org/droidtv/playtvapp/browse/SourceBrowser.java
1       1       source/TvApp/src/org/droidtv/playtvapp/data/Channel.java
5       0       source/TvApp/src/org/droidtv/playtvapp/data/Data.java
Author:Jonathan Jeurissen
Date:1428422271
Commit:2e99f048810717b7a49eb328cac95c87bc4691c8
Message:[ReacTV] Renamed resource file for build issue

-       -       source/ReacTV/res/drawable/App_icon_original.png
-       -       source/ReacTV/res/drawable/app_icon_original.png
Author:Jonathan Jeurissen

)
(defn get-commit-revision
  [line]
  (if-let [ret (clojure.string/split line #":")]
    (if (= (nth ret 0) "Commit")
      (nth ret 1))))
(defn get-commit-author
  [line]
  (if-let [ret (clojure.string/split line #":")]
    (if (= (nth ret 0) "Author")
      (nth ret 1))))
(defn get-commit-date
  [line]
  (if-let [ret (clojure.string/split line #":")]
    (if (= (nth ret 0) "Date")
      (nth ret 1))))
(defn get-commit-message
  [line]
  (if-let [ret (clojure.string/split line #":")]
    (if (= (nth ret 0) "Message")
      (nth ret 1))))

;;; return add delete file vector
(defn get-change-file
  [line]
  (if-let [ret (clojure.string/split line #"\t")]
    ret))


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
  [a]
  ())

(defn count-by-author-change-line
  [b]
  ())

