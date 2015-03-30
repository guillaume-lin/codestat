(ns codestat.core
  (:gen-class))

(use 'codestat.util)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println args)
  (run-cmd "cmd.exe" "dir"))


