(ns taoclj.foundation.connecting
  (:import [com.zaxxer.hikari HikariDataSource]
           [com.impossibl.postgres.jdbc PGDataSource]))




;; ; setup HikariCP pooled datasource
;; (def datasource
;;   (doto (HikariDataSource.)
;;         (.setDataSourceClassName "com.impossibl.postgres.jdbc.PGDataSource")
;;         (.setConnectionTimeout 5000)
;;         (.setMaximumPoolSize 3)
;;         (.addDataSourceProperty "Host"      "localhost")
;;         (.addDataSourceProperty "Port"      5432)
;;         (.addDataSourceProperty "Database"  "portal_db")
;;         (.addDataSourceProperty "User"      "portal_app")
;;         (.addDataSourceProperty "Password"  "dev")
;;         ;(.addDataSourceProperty "cachePrepStmts", "true");
;;         ;(.addDataSourceProperty "prepStmtCacheSize", "250");
;;         ;(.addDataSourceProperty "prepStmtCacheSqlLimit", "2048");
;;         ;(.addDataSourceProperty "useServerPrepStmts", "true");
;;     ))



;; ; using a direct datasource from the postgresql driver
;; (def datasource2
;;   (doto (PGDataSource.)
;;         (.setHost "localhost")
;;         (.setPort 5432)
;;         (.setDatabase "portal_db")
;;         (.setUser "portal_app")
;;         (.setPassword "dev")))





;; ; Using clojure.java.jdbc to process multiple result sets from a SQL Server instance.
;; ; https://gist.github.com/codelahoma/5716957

;; (defn execute
;;   ([cnx sql] (execute [] cnx sql))
;;   ([rs cnx sql]

;;    (let [statement  (.createStatement cnx)
;;          has-result (.execute statement sql)]

;;      (if-not has-result ; was a result set returned?
;;        (do (.close statement)
;;            true)

;;        ;otherwise we have a result set to load!
;;        [:get-resultset=
;;         (doall (resultset-seq (.getResultSet statement)))

;;         :get-more-results=
;;         (.getMoreResults statement)

;;         :get-resultset2=
;;         (doall (resultset-seq (.getResultSet statement)))

;;         ]

;;        )


;;      ) ) )


;; (with-open [cnx (.getConnection datasource2)]
;;   (execute cnx "select * from users where id = 192;select * from user_roles where user_id = 192;")
;;   )


;; (with-open [cnx (.getConnection datasource2)]
;;   (execute cnx "update users set name='Bill1' where id=192;"))


























