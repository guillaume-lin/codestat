(defproject codestat "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [korma "0.4.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [mysql/mysql-connector-java "5.1.35"]
                 [clj-webdriver "0.6.1"]
                 [org.clojure/data.json "0.2.6"]
                 [enlive "1.1.5"]	]
  :main ^:skip-aot codestat.core
  :target-path "target/%s"
  :plugins [[lein-ring "0.9.3"]]
  :ring {:handler codestat.web/handler}
  :profiles {:uberjar {:aot :all}})
