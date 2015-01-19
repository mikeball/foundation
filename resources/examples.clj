



;; (def tests-db
;;   (doto (PGDataSource.)
;;         (.setHost     "localhost") ; todo move into
;;         (.setPort     5432)
;;         (.setDatabase "foundation_tests")
;;         (.setUser     "foundation_tests_user")
;;         (.setPassword "password")))



;; ; def-postgres, def-postgres-nopool ? create a java object with function that aquires an connection?
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
;;         ; config.addDataSourceProperty("cachePrepStmts", "true");
;;         ; config.addDataSourceProperty("prepStmtCacheSize", "250");
;;         ; config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
;;         ; config.addDataSourceProperty("useServerPrepStmts", "true");
;;     ))
