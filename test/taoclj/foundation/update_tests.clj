(ns taoclj.foundation.update-tests
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]
            [taoclj.foundation.execution :refer [execute]]))



(deftest update-records

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS update_records;")
    (execute cnx "CREATE TABLE update_records (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO update_records (name) values ('bob'),('bill');"))

  (trx-> tests-db
         (update :update-records {:name "joe"} {:id 2}))

  (is (= [{:id 2 :name "joe"}]
         (qry-> tests-db
                (execute "SELECT id, name FROM update_records where id=2;"))))

  )


(deftest update-records-arrays

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS update_records_arrays;")
    (execute cnx (str "CREATE TABLE update_records_arrays (id serial primary key not null,"
                      "   names text[] not null, numbers integer[] not null);"))
    (execute cnx (str "INSERT INTO update_records_arrays (names,numbers)"
                      " values ('{\"bob\",\"bill\"}','{101,102}');"))
    )

  (trx-> tests-db
         (update :update-records-arrays
                 {:names ["john" "jane"] :numbers [505 606]}
                 {:id 1} ))

  (is (= [{:id 1 :names ["john" "jane"] :numbers [505 606]}]
         (qry-> tests-db
                (execute "SELECT id, names, numbers FROM update_records_arrays;"))  ))

  )
