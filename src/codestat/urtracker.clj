(ns codestat.urtracker)

;;;
;;; stat urtracker project here
;;;
(use 'clj-webdriver.taxi)
(defn test-selenium[]
 (do
   ;; Start up a browser
   (set-driver! {:browser :chrome} "https://github.com")
   (click "a[href*='login']")
   (input-text "#login_field" "your-username")
   (input-text "#password" "your-password")
   (submit "#password")
   (quit)))
