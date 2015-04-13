(ns codestat.web
  (:use (clojure.java io))
  (:use (codestat mysql git)))

;;;
;;; web interface for code stat
;;;
(defn get-body
	"get the body"
	[body]
	(let [ret (line-seq (reader body))]
		(spit (str "/tmp/" (gensym)) ret)
		ret))

;;;
;;; add a project to the database
;;;/project.add
;;;
(defn add-project
  [req]
  (insert-project 
    (->project-rec 
      0 "project_name" "project_desc"
      "vcs_url2" "vcs_login2" "vcs_pass2"
      "issue_url3" "issue_login3" "issue_pass3"))
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "add-project"})

;;;
;;; return project list
;;; /project.list
;;;
(defn get-project
  [req]
  (let [sb (StringBuffer.)]
    (.append sb "<html><body><table>")
    (doseq [p (query-project)]
      (-> sb (.append "<tr>")
        (.append  "<td>") (.append (:project_name p)) (.append "</td>") 
        (.append "<td>") (.append (:vcs_url p)) (.append "</td>") 
        (.append "<td>") (.append (:issue_url p)) (.append "</td>") 
        (.append "</tr>")))
    (.append sb "</table></body></html>")
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (.toString sb)}))

;;;
;;; define handler map
;;;
(def ^:dynamic *handler-map* 
  (hash-map "/project.list" get-project
            "/project.add" add-project))


;;;
;;; the dispatcher
;;;
(defn handler
   "ring handler"
   [req]
   (if-let [hlr (get *handler-map* (:uri req))]
     (hlr req)
   (let [ret (str "uri:" (:uri req) 
             "\nscheme:" (:scheme req)
             "\nrequest-method:" (:request-method req)
             "\nbody:" (get-body (:body req)))]

   {:status 200
    :headers {"Content-Type" "text/plain"}
    :body ret })))
  