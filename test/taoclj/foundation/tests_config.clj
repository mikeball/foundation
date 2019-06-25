(ns taoclj.foundation.tests-config
  (:import [com.impossibl.postgres.jdbc PGDataSource]))


(def tests-db
  (doto (PGDataSource.)
        (.setServerName    "localhost") ; todo move into
        (.setPort          5432)
        (.setDatabaseName  "foundation_tests")
        (.setUser          "foundation_tests_user")
        (.setPassword      "password")))

