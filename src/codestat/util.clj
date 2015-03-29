(ns codestat.util
  (:use (clojure.java shell)))



(defn run-cmd
  "run cmd in shell"
  [cmd & args]
  (println (:out (sh cmd))))
