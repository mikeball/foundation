(defproject org.taoclj/foundation "0.1.3"

  :description "A clojure data access library for postgresql."

  :url "https://github.com/mikeball/foundation"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.impossibl.pgjdbc-ng/pgjdbc-ng "0.8.2"]
                 [com.zaxxer/HikariCP "3.3.1"]
                 [cheshire "5.8.1"]])

