(ns taoclj.foundation.type-conversion-tests
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]
            [taoclj.foundation.execution :refer [execute]]))



(deftest handle-cast-to-integer
  (is (= '({:num 123})
          (qry-> tests-db
                 (execute "select '123'::int as num;")))))


(deftest handle-cast-to-text
  (is (= '({:txt "abc"})
          (qry-> tests-db
                 (execute "select 'abc'::text as txt;")))))




(deftest templated-casts-are-handled

  (def-query templated-casts-query
    {:file "taoclj/foundation/sql/templated_casts.sql"})

  (is (= '({:num 123})
         (qry-> tests-db
                (templated-casts-query {}))))

  )


