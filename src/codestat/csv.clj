(ns codestat.csv)
(use 'clojure.java.shell)  ;;; to import sh



;;;
;;;  save git stat record to .csv file
;;;  the format is as following:
;;;  ===== cut here =======
;;;  date, project, author, added-lines, deleted-lines,modified-files, commit , message
;;;  ===== cut here =======

(defrecord csv-record
  [date, project, author, added-lines, deleted-lines, modified-files, commit , message])



;;; 
;;; return a line-seq of git logs
;;;
(defn get-git-log
  [project-dir]
  (println "git log " project-dir)
  (line-seq (java.io.BufferedReader. 
              (java.io.StringReader. 
                (:out (with-sh-dir project-dir
                        (sh "git" "log" "--numstat" "--format=LOG%nAuthor:%an%nDate:%ai%nCommit:%H%nMessage:%s%nEND")))))))




(defn is-log?
  [line]
  (.startsWith line "LOG"))

;;;
;;; return list of log records
;;;
(defn get-log-records
  [lines ]
  (remove #(is-log? (first %))  
          (partition-by 
            is-log? 
            lines)))
;;;
;;;  deleted - 0, means added line
;;;  deleted - 1, means deleted line
;;;
(defn get-log-added-lines
  [lst deleted]
  (loop [added 0 ls lst]
    (if (empty? ls)
      added
      (recur (let [line (first ls)]
               (let [ret (clojure.string/split line #"\t" 3)]
                 (if (> (count ret) 2)
                   (if (= (nth ret 0) "-")
                     added
                     (+ added (Integer/parseInt (nth ret deleted))))
                   added))) 
        (rest ls)))))




;;;
;;; parse a log record from list with log information
;;; the log record is like:
;;; ("Author:jingxian.lin" "Date:1443060628" "Commit:93095c4a374ec3a4011b23676e93d63ba0be1695" 
;;;  "Message:update 1539.4 launcher and lock and music progress updated." "END" "" 
;;;  "-\t-\tplan/Philips Mobile project XMIC task tracking stage 2.xlsx")
;;; 
(defn parse-log-record
  [lst ]
  (->csv-record 
    (nth  (clojure.string/split (nth  lst 1) #"Date:" ) 1)
    "unknown"
    (nth (clojure.string/split (nth lst 0) #"Author:") 1)
    (get-log-added-lines lst 0)
    (get-log-added-lines lst 1)
    (- (count lst) 6) ;;; remove the log head, the other is modified files
    (nth (clojure.string/split (nth lst 2) #"Commit:") 1)
    (nth (clojure.string/split (nth lst 3) #"Message:") 1)))

;;;
;;; write the csv records 
;;;
;;;
(defn write-csv-records-to-file
  [records file]
  (with-open [*out* (java.io.FileWriter. file)]
    (doseq [rs records]
      (clojure.pprint/cl-format *out* 
                                "\"~d\",~s,~s,\"~d\",\"~d\",\"~d\",~s,~s\r\n" 
                                (:date rs)
                                (:project rs)
                                (:author rs)
                                (:added-lines rs)
                                (:deleted-lines rs)
                                (:modified-files rs)
                                (:commit rs)
                                (:message rs)))))
;;;
;;; get project records
;;;
(defn get-project-records
  [workspace project]
  (printf "get-project-records %s %s\n" workspace project)
  (map #(assoc % :project project);;; add project name to each record
    (map parse-log-record  
         (get-log-records 
           (get-git-log  
             (str workspace "/" project))))))

;;;
;;;  specify a workspace and write the records to file
;;;
(defn write-workspace-records-to-csv
  [workspace file]
  (let [projects (line-seq (java.io.BufferedReader. 
                             (java.io.StringReader.
                               (:out (with-sh-dir workspace
                                       (sh "ls"))))))]
    (println (first projects))
    (write-csv-records-to-file (reduce into
                                       (map 
                                        (partial get-project-records workspace) 
                                        projects)) file)))

;;;
;;; get all records from workspace
;;;
(defn get-workspace-records
  [workspace]
  (let [projects (line-seq (java.io.BufferedReader. 
                             (java.io.StringReader.
                               (:out (with-sh-dir workspace
                                       (sh "ls"))))))]
    (println (first projects))
    (reduce into
            (map 
              (partial get-project-records workspace) 
              projects))))


;;; return date like: 2015-09-18
(defn get-date
  [record]
  (subs (:date record) 0 10))
;;; transfer 2015-09-01 to 2015009001
(defn date2int
  [date]
  (Integer/parseInt (clojure.string/replace date #"-" "0")))
;;;
;;; noraml records should have less 10000 line added
;;; and date later than 2015-09-01
(defn is-valid-record?
  [record]
  (if (or (> (:added-lines record) 3000)
          (< (date2int (get-date record)) (date2int "2015-09-01")))
    false
    true))

(defn is-commit-in-date? 
  [date record]
  (if (or (= date "*")
          (= date (get-date record)))
    true
    false))

;;; get added/deleted lines
(defn get-modified-lines
  [records]
  (reduce #(+ %1 (:added-lines %2) (:deleted-lines %2)) 
          0 
          records))

;;; get line increments
(defn get-increment-lines
  [records]
  (reduce #(+ %1 (- (:added-lines %2) (:deleted-lines %2))) 
          0 
          records))


(defrecord date-lines-record
  [date lines])
;;;
;;; merge records by date
;;;
(defn merge-records-by-date
  [records functor]
  (let [dm (group-by get-date (filter (partial is-commit-in-date? "*") 
                                      (filter is-valid-record? records)))]
    (map #(->date-lines-record
             %1 
             (functor (get dm %1))) 
         (keys dm))))

;;;
;;; write date lines record to csv
;;;
(defn write-date-lines-record-to-csv
  [records file]
  (with-open [*out* (java.io.FileWriter. file)]
    (doseq [rs records]
      (clojure.pprint/cl-format *out* 
                                "~s,\"~d\"\r\n" 
                                (:date rs)
                                (:lines rs)))))

(defn write-workspace-dlr-to-csv
  [workspace file functor]
  (write-date-lines-record-to-csv
    (merge-records-by-date
      (get-workspace-records workspace functor))
    file))


(defn write-workspace-increment-dlr-to-csv
  [workspace file]
  (write-workspace-dlr-to-csv workspace file get-increment-lines))


;;; project's everyday progress
(defrecord project-date-lines-record
    [project date lines])

(defn get-project-and-date
  [record]
  {:project (:project record) :date (get-date record)})
  
(defn get-project-lines-records
  [workspace functor]
  (let [dm (group-by get-project-and-date  
            (filter is-valid-record? (get-workspace-records workspace)))]
    (map #(->project-date-lines-record
           (:project %1)
           (:date %1)
           (functor (get dm %1)))
         (keys dm))))
;;;
;;; project - loc
;;;
(defn write-workspace-plr-to-csv
  [workspace file functor]
  (with-open [*out* (java.io.FileWriter. file)]
    (doseq [rs (get-project-lines-records workspace functor)]
      (clojure.pprint/cl-format *out* 
                                "~s,~s,\"~d\"\r\n" 
                                (:project rs)
                                (:date rs)
                                (:lines rs)))))

(defn write-workspace-modified-lines-to-csv
  [workspace file ]
  (write-workspace-plr-to-csv workspace file get-modified-lines))

(defn write-workspace-increment-lines-to-csv
  [workspace file]
  (write-workspace-plr-to-csv workspace file get-increment-lines))

;;;
;;; count total line of project
;;;


    
(defn get-date-lines-records-of-project
  [records project ]
  "cumulate added-lines deleted-lines"
  (let [rs-1 (filter #(= project (:project %1))
                   records)
        rs-2 (group-by #(get-date %) rs-1)]
    (map #(->date-lines-record
           %1
           (get-increment-lines (get rs-2 %1)))
         (keys rs-2))))


