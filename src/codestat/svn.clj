(ns codestat.svn)
(use 'clojure.java.shell)
(require  '[codestat.mysql :as mysql])

(def ^:dynamic *workspace* "/home/jenkins/workspace")

(defn get-base-svn-dir
  [project-url]
  (last (clojure.string/split project-url #"/" )))


(defn get-full-svn-dir
  "get the full dir of svn"
  [project-url]
  (str *workspace* "/" (get-base-svn-dir project-url)))


(defn svn-checkout
  "clone the svn project"
  [project-url]
  (with-sh-dir *workspace*
    (sh "svn" "checkout" project-url)))
  
(defn svn-update
  "make the code up to date"
  [project-url]
  (with-sh-dir (get-full-svn-dir project-url)
    (sh "svn" "update")))


;;;
;;; make the directory code up to date
;;;
(defn update-svn-code
  "check if the project dir is exist, if not , checkout, then update"
  [project-url]
  (if (.isExist (java.io.File. (get-full-svn-dir project-url)))
    (svn-checkout project-url)
    (svn-update project-url)))

;;;
;;; svn log -v --xml
;;; get all the commit information
(comment " 
<?xml version="1.0" encoding="UTF-8"?>
<log>
<logentry
   revision="7389">
<author>xiaohui.yu</author>
<date>2015-03-04T09:09:03.648923Z</date>
<paths>
<path
   action="M"
   kind="file">/Ebony_2K15/Ebony2K15_Apps/ebony2K15_EPG.apk</path>
</paths>
<msg></msg>
</logentry> "
)

(require '[clojure.data.xml :as xml])
;;;
;;;
;;;
(defn svn-log
  "return sequence of logentry"
  [project-url]
  (println "svn log " project-url)
  (filter #(= (:tag %) :logentry)
          (let [xml-text (:out (with-sh-dir (get-full-svn-dir project-url)
                         (sh "svn" "log" "--xml")))]
           (let [root (xml/parse-str xml-text)]
             (xml-seq root)))))


(defn timestamp-to-milliseconds
  " convert string like '2014-07-18T02:10:54.370281Z' to seconds since 1970 ..."
  [string]
  (+ (* 8 60 60 1000) ;;; add 8 hours to be look like beijing time 
  (.getTime 
       (java.sql.Timestamp/valueOf 
         (clojure.string/replace 
           (clojure.string/replace string "T" " ") "Z" "")))))
  

(defn get-author-name
  "input is log entry"
  [logentry]
  (first (:content (first (:content logentry)))))

(defn get-commit-date
  [logentry]
  (timestamp-to-milliseconds 
    (first 
      (:content 
        (second 
          (:content logentry))))))

(defn get-commit-revision
  [logentry]
  (str "r" (:revision (:attrs logentry))))

(defn get-commit-messge
  [logentry]
  (first (:content (last (:content logentry)))))


(defn parse-commit
  [logentry]
  (mysql/->commit-rec (get-author-name logentry)
                (get-commit-date logentry)
                (get-commit-revision logentry)
                (get-commit-messge logentry)))

(defn parse-all-commits
  [project-url]
  (map parse-commit (svn-log project-url)))
;;;
;;;  svn diff -c r<v>
;;;  ex: svn diff -c r20
;;;
;;;  return line of diff
(defn svn-diff
  [project-url revision]
  (line-seq (java.io.BufferedReader. 
              (java.io.StringReader. 
                (:out (with-sh-dir (get-full-svn-dir project-url)
                        (sh "svn" "diff" "-c" revision)))))))

(comment "
Index: product/mobile/MyRemote_android_2k14/source/MyRemote_android_2k14/AndroidManifest.xml
===================================================================
--- product/mobile/MyRemote_android_2k14/source/MyRemote_android_2k14/AndroidManifest.xml       (revision 7577)
+++ product/mobile/MyRemote_android_2k14/source/MyRemote_android_2k14/AndroidManifest.xml       (revision 7578)
@@ -2,8 +2,8 @@
 <manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"
     android:installLocation=\"auto\"
     package=\"com.tpv.android.apps.tvremote\"
-    android:versionCode=\"20150215\"
-    android:versionName=\"0.047.7575\" >
+    android:versionCode=\"20150227\"
+    android:versionName=\"0.048.7577\" >
     <uses-sdk
         android:minSdkVersion=\"10\"/>
     <uses-permission android:name=\"android.permission.INTERNET\" />
"
  )
(defn collect-add-line
  [lines]
  (count (filter #(.startsWith % "+   ") lines)))
  
(defn collect-delete-line
  [lines]
  (count (filter #(.startsWith % "-   ") lines)))

(defn parse-svn-changeset
  "return a change-rec"
  [file changes]
  (mysql/->change-rec (collect-add-line changes) 
                (collect-delete-line changes)
                (last (clojure.string/split file #"Index: " 2))))


(defn is-index?
  [line]
  (.startsWith line "Index:"))

(defn parse-diff-record
  "return seq of change-rec"
  [lines]
  (for [cs  (partition 2 (partition-by is-index? lines))]
    (let [file (first (first cs)) changes (second cs)]
      (parse-svn-changeset file changes))))

(defn parse-revision-changeset
  "return seq of change-rec"
  [project-url revision]
  (parse-diff-record (svn-diff project-url revision)))

  
(defn collect-change-log 
  "collect change log for svn project"
  [project-url]
  (for [cmt (parse-all-commits project-url)]
    (let [rec (mysql/->log-rec
                cmt
                (parse-revision-changeset project-url (:revision cmt)))]
      rec)))

(defn insert-all-svn-log
  [project-url ]
  (doseq [log (collect-change-log project-url)]
    (mysql/insert-log log project-url "svn")))