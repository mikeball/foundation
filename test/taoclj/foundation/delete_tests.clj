(ns taoclj.foundation.delete-tests
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]
            [taoclj.foundation.execution :refer [execute]]))




(deftest delete-records

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS delete_records;")
    (execute cnx "CREATE TABLE delete_records (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO delete_records (name) values ('bob'),('bill');"))

  (trx-> tests-db
         (delete :delete-records {:id 1}))

  (is (= [{:id 2 :name "bill"}]
         (qry-> tests-db
                (execute "SELECT id, name FROM delete_records;"))))

  )
