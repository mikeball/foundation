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


(deftest update-nulls
  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS update_nulls;")
    (execute cnx (str "CREATE TABLE update_nulls (id serial primary key not null, "
                      "  t text, i integer, b boolean, tz timestamptz);"))
    (execute cnx "INSERT INTO update_nulls (t,i,b,tz) values ('a',1,true,'2016-02-05');"))

  (trx-> tests-db
         (update :update-nulls {:t nil :i nil :b nil :tz nil} {:id 1}))

  (is (= [{:id 1 :t nil :i nil :b nil :tz nil}]
         (qry-> tests-db
                (execute "SELECT * FROM update_nulls where id=1;"))))
  )


(deftest insert-nulls

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS insert_nulls;")
    (execute cnx (str "CREATE TABLE insert_nulls (id serial primary key not null, "
                      "  t text, i integer, b boolean, tz timestamptz);")))

  (trx-> tests-db
         (insert :insert-nulls {:t nil :i nil :b nil :tz nil}))

  (is (= [{:id 1 :t nil :i nil :b nil :tz nil}]
         (qry-> tests-db
                (execute "SELECT * FROM insert_nulls;"))  ))

  )


(deftest update-records-of-all-types
  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS update_records_of_all_types;")
    (execute cnx "CREATE TABLE update_records_of_all_types (id serial primary key not null,
             i integer, b boolean, t text, tsz timestamptz);")

    (execute cnx "INSERT INTO update_records_of_all_types (i,b,t,tsz)
             values (null,null,null,null);"))

  (let [now (java.time.Instant/now)]
    (trx-> tests-db
           (update :update-records-of-all-types
                   {:i 101 :b true :t "abc" :tsz now}
                   {:id 1}))

    (is (= [{:id 1 :i 101 :b true :t "abc" :tsz now}]
           (qry-> tests-db
                  (execute "SELECT id,i,b,t,tsz FROM update_records_of_all_types where id=1;")))))
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
