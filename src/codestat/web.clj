(ns codestat.web
  (:use (clojure.java io)))

;;;
;;; web interface for code stat
;;;
(defn get-body
	"get the body"
	[body]
	(let [ret (line-seq (reader body))]
		(spit "/tmp/req" ret)
		ret))


(defn handler
   "ring handler"
   [req]
   (let [ret (str "uri:" (:uri req) 
                  "\nscheme:" (:scheme req)
                  "\nrequest-method:" (:request-method req)
                  "\nbody:" (get-body (:body req)))]

   {:status 200
    :headers {"Content-Type" "text/plain"}
    :body ret 
	}))
