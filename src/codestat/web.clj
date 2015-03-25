(ns codestat.web)

;;;
;;; web interface for code stat
;;;
(defn get-body
	"get the body"
	[body]
	(binding [*in* body]
	(loop [input1 (read-line)]
		
		(if input1 (recur (read-line)) (str input1)))))


(defn handler
   "ring handler"
   [req]
   (let [ret (str "uri:" (:uri req) 
     	"\nscheme:" (:scheme req)
	"\nrequest-method:" (:request-method req)
	)]
	(spit "/tmp/test" (get-body (:body req)) )
   {:status 200
    :headers {"Content-Type" "text/plain"}
    :body ret 
	}))
