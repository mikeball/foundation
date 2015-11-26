(ns taoclj.foundation.json-tests
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]
            [taoclj.foundation.execution :refer [execute]]))



(deftest read-json-columns
  (is (= '({:person {"name" "bob"}})
          (qry-> tests-db
                 (execute "select '{\"name\" : \"bob\"}'::json as person;")))))




; json templated select ?



; json insert



; json update







;; (run-tests *ns*)


;; (qry-> tests-db
;;        (execute "select '{\"name\" : 123}'::json as person;")
;; )
