(ns taoclj.foundation.datasources
  (:import [com.zaxxer.hikari HikariDataSource]
           [com.impossibl.postgres.jdbc PGDataSource]))



(defn create-datasource [config]
  (doto (PGDataSource.)
        (.setHost     (:host config))
        (.setPort     (:port config))
        (.setDatabase (:database config))
        (.setUser     (:username config))
        (.setPassword (:password config))))


;; (create-datasource {:host "localhost"
;;    :port 5432
;;    :database "foundation_tests"
;;    :username "foundation_tests_user"
;;    :password "password"
;;    })


(defn create-pooled-datasource [config]
  (doto (HikariDataSource.)
        (.setDataSourceClassName "com.impossibl.postgres.jdbc.PGDataSource")

        (.addDataSourceProperty "Host"      (:host config))
        (.addDataSourceProperty "Port"      (:port config))
        (.addDataSourceProperty "Database"  (:database config))
        (.addDataSourceProperty "User"      (:username config))
        (.addDataSourceProperty "Password"  (:password config))

        (.setConnectionTimeout 5000)
        (.setMaximumPoolSize 3)
        ;(.addDataSourceProperty "cachePrepStmts", "true");
        ;(.addDataSourceProperty "prepStmtCacheSize", "250");
        ;(.addDataSourceProperty "prepStmtCacheSqlLimit", "2048");
        ;(.addDataSourceProperty "useServerPrepStmts", "true");

    ))




