(defproject org.taoclj/foundation "0.1.3"

  :description "A clojure data access library for postgresql."

  :url "https://github.com/mikeball/foundation"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.impossibl.pgjdbc-ng/pgjdbc-ng "0.6"]
                 [com.zaxxer/HikariCP "2.4.3"]
                 [cheshire "5.5.0"]])

