(defproject org.taoclj/foundation "0.1.0-SNAPSHOT"
  :description "FIXME: write description"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0-RC2"]
                 [com.impossibl.pgjdbc-ng/pgjdbc-ng "0.6"]
                 [com.zaxxer/HikariCP "2.4.2"]


                 ; for some reason this was required to remove
                 ; an log to console on each startup.
                 [org.slf4j/slf4j-simple "1.7.13"]
                 ])
