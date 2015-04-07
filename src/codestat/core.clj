(ns codestat.core
  (:gen-class))

(use 'codestat.util)
(use 'codestat.urtracker)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println args)
  (condp = (first args)
    "test" (println "test")
    "login" (login "jingxian.lin" "#e4r5t" "http://fwtrack.tpvaoc.com")
    ))


