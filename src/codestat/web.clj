(ns codestat.web
  (:use (clojure.java io))
  (:use (codestat mysql git)))

(require '[net.cgrand.enlive-html :as html])

(defprotocol SerialNo
  (reset [this])
  (next [this]))
(deftype SerialNoGenerator [ ^:volatile-mutable serial-no ]
  SerialNo
  (reset [this] (set! serial-no 0))
  (next [this] (set! serial-no (inc serial-no))))
 

(html/deftemplate add-project-templ "templates/add-project.html"
  [{:keys [path]}]
  [:head :title ](html/content "welcome to add project."))

;;; fill the second :tr with project list
(html/deftemplate list-project-templ "templates/list-project.html"
  [recs ]
  [:body :table [:tr (html/nth-of-type 2)]]
  (let [sg (->SerialNoGenerator 0)]
    (html/clone-for [rec recs ](html/html-content 
                                 (str "<tr>"
                                      "<td>" (.next sg) "</td>"
                                      "<td>" (:project_name rec) "</td>"
                                      "<td>" (:project_desc rec) "</td>"
                                      "<td>" (:vcs_url rec) "</td>"
                                      "<td>" (:issue_url rec) "</td>"
                                      "</tr>")))))

(defn sort-by-count
  [coll]
  (sort #(compare (first (vals %2)) (first (vals %1)))
        coll))


;;; count aspect of projects
(html/deftemplate count-project-templ "templates/count-project.html"
  [project-recs commit-recs changeline-recs]
  [:body :table#count_project_by_author [:tr (html/nth-of-type 2)]]
  (let [sg (->SerialNoGenerator 0)]
    (html/clone-for [prec (sort-by-count project-recs)] 
                    (html/html-content
                      (str "<tr>"
                           "<td>"  (.next sg)         "</td>"
                           "<td>" (first (keys prec)) "</td>"
                           "<td>" (first (vals prec)) "</td>"
                           "</tr>"))))
  [:body :table#count_commit_by_author [:tr (html/nth-of-type 2)]]
  (let [sg1 (->SerialNoGenerator 0)]
    (html/clone-for [crec (sort-by-count commit-recs)] 
                    (html/html-content
                      (str "<tr>"
                           "<td>"  (.next sg1)         "</td>"
                           "<td>" (first (keys crec)) "</td>"
                           "<td>" (first (vals crec)) "</td>"
                           "</tr>"))))
  [:body :table#count_change_line_by_author [:tr (html/nth-of-type 2)]]
  (let [sg2 (->SerialNoGenerator 0)]
    (html/clone-for [cl-rec (sort-by-count changeline-recs)] 
                    (html/html-content
                      (str "<tr>"
                           "<td>"  (.next sg2)         "</td>"
                           "<td>" (first (keys cl-rec)) "</td>"
                           "<td>" (first (vals cl-rec)) "</td>"
                           "</tr>")))))
  
;;;
;;; web interface for code stat
;;;
(defn get-body
	"get the body"
	[body]
	(let [ret (line-seq (reader body))]
		(spit (str "/tmp/" (gensym)) ret)
		ret))

;;; "a=b"  -> {:a b}
(defn get-map 
  [line]
  (let [pair (clojure.string/split line #"=")]
    (assoc {} (keyword (first pair)) (second pair))))

;;; get the query map
(defn get-qmap-from-string
  [line]
  (println "get-qmap-from-string: " line)
  (reduce merge (map get-map (clojure.string/split line #"&"))))

(defn get-add-project-input
  [line ]
  (let [rec (get-qmap-from-string line)]
    (->project-rec 0 (:project_name rec) (:project_desc rec) 
                   (:vcs_url rec) (:vcs_login rec) (:vcs_pass rec) 
                   (:issue_url rec) (:issue_login rec) (:issue_pass rec))))

;;;
;;; add a project to the database
;;;/project.add
;;;
(defn add-project
  [req]
  (println "add-project with request-method " (:request-method req))
  (if (= (:request-method req) :post)
    ;add project
    (let [rec (get-add-project-input (first (get-body (:body req))))]
      (insert-project rec)
      {:status 301
       :headers {"Location" "/list-project.html"}
       :body "added"})
    ;else show input form
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (add-project-templ "")}))

;;;
;;; return project list
;;; /project.list
;;;
(defn list-project
  [req]
  (let [recs (query-project)]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (apply str (list-project-templ recs))}))


;;;
;;; /count-project.html
;;; stat the project
(defn count-project
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (let [project-rec (count-project-by-author) 
         commit-rec (count-commit-by-author) 
         cl-rec (count-change-line-by-author)]
   (apply str (count-project-templ project-rec commit-rec cl-rec)))})

;;;
;;; define handler map
;;;
(def ^:dynamic *handler-map* 
  (hash-map "list-project.html" list-project
            "count-project.html" count-project
            "add-project.html" add-project))


;;;
;;; the dispatcher
;;;
(defn handler
   "ring handler"
   [req]
   (if-let [hlr (get *handler-map* (last (clojure.string/split (:uri req) #"/")))]
     (hlr req)
   (let [ret (str "uri:" (:uri req) 
             "\nscheme:" (:scheme req)
             "\nrequest-method:" (:request-method req)
             "\nbody:" (get-body (:body req)))]

   {:status 200
    :headers {"Content-Type" "text/plain"}
    :body ret })))
  