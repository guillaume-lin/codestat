(ns codestat.core
  (:gen-class))

(use 'codestat.util)
(use 'codestat.urtracker)
(use 'codestat.git)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println args)
  (condp = (first args)
    "test" (println "test")
    "login" (login "jingxian.lin" "#e4r5t" "http://fwtrack.tpvaoc.com")
    "collect" (collect-all-git-log "http://xmicgit.tpvaoc.com")
    ))


