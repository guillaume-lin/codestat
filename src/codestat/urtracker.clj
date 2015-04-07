(ns codestat.urtracker)

;;;
;;; stat urtracker project here
;;;
;;; System.setProperty("webdriver.chrome.driver", "D:/lib/selenium-2.44.0/chromedriver.exe");
;;;
(System/setProperty "webdriver.chrome.driver" "D:/lib/selenium-2.44.0/chromedriver.exe")

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

; login to http://fwtrack.tpvaoc.com
(defn login
  [user pass project-url]
  (do
    (set-driver! {:browser :chrome} project-url)
    (input-text "#txtEmail" user)
    (input-text "#txtPassword" pass)
    (submit "#btnLogin")))

(defn get-issue-page
  [issue-no]
  ())
