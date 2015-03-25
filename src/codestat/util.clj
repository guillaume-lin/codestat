(ns codestat.util
  (:use (clojure.java shell)))



(defn run-cmd
  "run cmd in shell"
  [cmd & args]
  ((sh cmd)
  (println args)))
