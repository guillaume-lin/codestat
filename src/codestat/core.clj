(ns codestat.core
  (:gen-class))
(use 'clj-webdriver.taxi)
(use 'codestat.util)
(defn test-selenium[]
 (do
   ;; Start up a browser
   (set-driver! {:browser :chrome} "https://github.com")
   (click "a[href*='login']")
   (input-text "#login_field" "your-username")
   (input-text "#password" "your-password")
   (submit "#password")
   (quit)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println args)
  (run-cmd "cmd.exe" "dir"))


