(ns taoclj.foundation.tests-config
  (:import [com.impossibl.postgres.jdbc PGDataSource]))


(def tests-db
  (doto (PGDataSource.)
        (.setHost     "localhost") ; todo move into
        (.setPort     5432)
        (.setDatabase "foundation_tests")
        (.setUser     "foundation_tests_user")
        (.setPassword "password")))

