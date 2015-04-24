(ns codestat.git)
(use 'clojure.java.shell)  ;;; to import sh
(use 'codestat.mysql) ;;; import insert-log

;;;==========================================
;;; data extraction and data translation
;;;==========================================

(def ^:dynamic *workspace* "/home/jenkins/workspace")
(def ^:dynamic *debug* false)
;;; return the final part of the git project url
(defn get-base-git-dir
  [project-url]
  (first 
    (clojure.string/split
      (last 
        (clojure.string/split project-url #"/")) #"\.")))
;;;
(defn get-full-git-dir
  [project-url]
  (str *workspace* "/" 
       (get-base-git-dir project-url)))
;;;
(defn line-seq-from-string
  [str]
  (line-seq 
    (java.io.BufferedReader. 
      (java.io.StringReader. str))))
  
;git@172.20.30.2:aoc-2k15-app/aoc-2k15-launcher.git
(defn git-clone
  [project-url]
  (println "git clone " project-url)
  (with-sh-dir *workspace*
    (sh "git" "clone" project-url)))
  
;;;
(defn git-pull
  [project-url]
  (println "git pull " project-url)
  (with-sh-dir (get-full-git-dir project-url) 
    (sh "git" "pull")))
;;;
(defn git-checkout-branch
  [project-url branch]
  (println "checkout " branch " from " project-url)
  (with-sh-dir (get-full-git-dir project-url)
    (sh "git" "checkout" branch)))

;
(defn update-git-code
  [project-url]
  (let [dir (get-full-git-dir project-url)]
    (if 
      (.exists 
        (java.io.File. dir))
      (git-pull project-url)
      (git-clone project-url))))
;
(defn get-git-branches
  [project-url]
  (distinct (map 
              #(.substring % (+ 7 (.lastIndexOf % "origin/"))) 
                 (line-seq-from-string 
                   (:out 
                     (with-sh-dir 
                       (get-full-git-dir project-url)
                       (sh "git" "branch" "-r")))))))
    
;;; 
;;; return a line-seq of git logs
;;;
(defn git-log
  [project-url]
  (println "git log " project-url)
  (line-seq (java.io.BufferedReader. 
              (java.io.StringReader. 
                (:out (with-sh-dir (get-full-git-dir project-url)
                        (sh "git" "log" "--numstat" "--format=LOG%nAuthor:%an%nDate:%at%nCommit:%H%nMessage:%s%nEND")))))))

;;; 
;;; log format as:
;;; git log --numstat --format=LOG%nAuthor:%an%nDate:%at%nCommit:%H%nMessage:%s%nEND"
;;;
(comment "
LOG
Author:lx.mao
Date:1428461520
Commit:85fa48a1409bb22b70b9d4d6173766d7777e741d
Message:[TvApp]Show channel logo and current program's thumbnail on channel list.
END

6       0       source/TvApp/res/layout/channel_item.xml
4       2       source/TvApp/res/values/dimens.xml
8       3       source/TvApp/src/org/droidtv/playtvapp/browse/ChannelAdapter.java
20      16      source/TvApp/src/org/droidtv/playtvapp/browse/ChannelItemView.java
0       7       source/TvApp/src/org/droidtv/playtvapp/browse/SourceBrowser.java
1       1       source/TvApp/src/org/droidtv/playtvapp/data/Channel.java
5       0       source/TvApp/src/org/droidtv/playtvapp/data/Data.java
LOG
Author:Jonathan Jeurissen
Date:1428422271
Commit:2e99f048810717b7a49eb328cac95c87bc4691c8
Message:[ReacTV] Renamed resource file for build issue
END

-       -       source/ReacTV/res/drawable/App_icon_original.png
-       -       source/ReacTV/res/drawable/app_icon_original.png
Author:Jonathan Jeurissen"

)
;;; "Author:xxxx"
(defn get-commit-field
  [lst field]
  (loop  [ls lst]
    (if-let [line (first ls)]
      (if-let [ret (clojure.string/split line #":" 2)]
        (if (= (nth ret 0) field)
          (nth ret 1)
          (recur (rest ls)))))))


(defn- find-index-of
  [s lst]
  (loop [idx 0 ls lst]
    (if (.startsWith (first ls) s)
      idx
      (recur (inc idx) (rest ls)))))
  
(defn- get-message-start
  [lst]
  (find-index-of "Message:" lst))
(defn- get-message-end
  [lst]
  (find-index-of "END" lst))

(defn- remove-message-head
  [s]
  (last (clojure.string/split s #"Message:" 2)))

;;; "Message:xxxx" "..." ... "END" ""
(defn get-commit-message
  [lst]
  (let [s (get-message-start lst) e (get-message-end lst)]
    (remove-message-head (apply str (map #(str % "\n") 
                                         (drop s (take e lst))
                                         )))))

;;; return [add delete file] vector
(defn get-change-file
  [line]
  (if-let [ret (clojure.string/split line #"\t")]
    ret))

(defn is-log?
  [line]
  (.startsWith line "LOG"))

(defn get-log-seq
  [project-url ]
  (remove #(is-log? (first %))  
          (partition-by 
            is-log? 
            (git-log project-url))))

(defrecord commit-rec
  [author_name commit_date revision message])
(defrecord change-rec
  [add_line delete_line file])
(defrecord log-rec
  [commit-rec changeset-rec])


;;; lst is like: ("Author:..." "Date:..." ....""...)
(defn parse-log-head
   [lst ]
   (->commit-rec (get-commit-field lst "Author")
                 (get-commit-field lst "Date")
                 (get-commit-field lst "Commit")
                 (get-commit-message lst)))

;;; "-" "1" ->  0 1
(defn get-line-count
  [s]
  (if (= 0 (compare s "-"))
    0
    (Integer/parseInt s)))
;;; "0 0 file"  ->  record[0 0 "file"]
(defn parse-change-file
  [line]
  (if-let [ret (clojure.string/split line #"\t")]
    (if (= 3 (count ret))
      (->change-rec (get-line-count (nth ret 0))
                    (get-line-count (nth ret 1))
                    (nth ret 2)))))

;;; ("Author" ... "0 0 file"
(defn parse-changeset
  [lst]
  (loop [cs [] ls lst]
    (if (empty? ls)
      cs
    (if-let [csf (parse-change-file (first ls))]
      
      (recur (conj cs csf) (rest ls))
      (recur cs (rest ls))))))
    
;;; get log-rec record
(defn parse-log-rec
  [lst]
(->log-rec (parse-log-head lst)
           (parse-changeset (drop (+ 2 (find-index-of "END" lst)) lst))))
  
;;; parse output from git log
(defn parse-git-log
  [project-url]
  (for [lst (get-log-seq project-url)]
    (parse-log-rec lst)))

(defn insert-git-log
  [project-url branch]
  (doseq [log (parse-git-log project-url)](insert-log log project-url branch)))


;;; count change line of record
(defn count-change-line-of-rec
  [rec]
  (+ (reduce + (map :add-line (:changeset-rec rec)))
     (reduce + (map :delete-line (:changeset-rec rec)))))
;;;
(defn count-change-line-of-log
  [project-url]
  (reduce + (map count-change-line-of-rec (parse-git-log project-url))))



;;; record for project
(defrecord project-rec
  [id project_name project_desc
   vcs_url vcs_login vcs_pass
   issue_url issue_login issue_pass])

;;;
;;; read project configuration from database
;;; start collect git log to database
;;;
(defn collect-git-log
  [project-url work-dir]
  (binding [*workspace* work-dir]
    (update-git-code project-url)
    (doseq [brch (get-git-branches project-url)]
      (git-checkout-branch project-url brch)
      (insert-git-log project-url brch))))


;;; 
;;; search commit table for this info
;;; return seq of map [:author :project_id]
(defn count-project-by-author
  []
  (map (fn [e] {(key e) (count (val e))})
       (group-by :author_name 
                 (distinct 
                   (map #(select-keys % [:author_name :project_id])
                        (query-commit))))))
         


;;;
;;; search commit table for this info
;;; return seq of map [:author :commit]
;;;
(defn count-commit-by-author
  []
  (map (fn[e] {(key e) (count (val e))})
       (group-by :author_name 
                 (distinct 
                   (map #(select-keys % [:author_name :id])
                        (query-commit))))))
 

(defn- get-author-by-commit-id
  [commit-id]
  (:author_name (first 
                  (query-author-by-commit-id commit-id))))

(defn get-author-change-line-map
  []  
  (group-by #(first (keys %))
            (map (fn [e]
                   {(get-author-by-commit-id (first (keys e))) (first (vals e))})
                 (map (fn[e](hash-map (key e) (reduce + 
                                                      (map (fn[r] (+ (:add_line r) (:delete_line r))) (val e)))))
                      (group-by :commit_id 
                                (map #(select-keys % [:commit_id :add_line :delete_line])
                                     (query-changeset)))))))

;;; search commit and changeset table for this info
;;; return seq of map with keys [:author  :change-line]
(defn count-change-line-by-author
  []
  (let [kk (get-author-change-line-map)]
    (for [k (keys kk)] {k (reduce + (map #(% k) (kk k)))})))
  

(require '[net.cgrand.enlive-html :as html])
(require '[clojure.data.json :as json])

;;; private token for gitlab
(def PRIVATE-TOKEN "we7YH2bXscT7MsEAs8t5")

(defn get-project-page
  [gitlab-server page-no]
  (html/html-resource (java.net.URL. (str gitlab-server 
                                          "/api/v3/projects?"
                                          "private_token="
                                          PRIVATE-TOKEN
                                          "&page="
                                          page-no))))
(defn page-to-json
  [page]
  (json/read-str (first (:content (first (:content (first page)))))))

;;;
;;; get project list from gitlab server
;;;
(defn get-project-list
  [gitlab-server]
  (loop [v [] page 1]
    (let  [pg (get-project-page gitlab-server page)
           js (page-to-json pg)]
      (if (> (count js) 0)
        (recur (into v js)  (inc page))
        v))))

(def active-project-set
  (hash-set 
    "ots-flagship-2k15"
    "aoc-2k15-app"
    "ebony-2k15-app"
    "mobile"
    "innovation"
    ))

(defn active-project?
  [name]
  (contains? active-project-set 
             (.substring name (+ 1 (.indexOf name ":")) (.indexOf name "/")) ))
(defn get-project-git-list
  [project-list]
  (filter active-project? (map #(get % "ssh_url_to_repo") project-list)))



(defn collect-all-git-log
  [gitlab-server]
  (doseq [project (get-project-git-list (get-project-list gitlab-server))]
    (collect-git-log project "/tmp/ws")))

