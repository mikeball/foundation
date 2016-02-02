(ns taoclj.foundation.select-tests
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]
            [taoclj.foundation.execution :refer [execute]]))


(deftest select1-record

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS select1_record;")
    (execute cnx "CREATE TABLE select1_record (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO select1_record (name) VALUES('bob');"))


  (is (= {:id 1 :name "bob"}
         (qry-> tests-db
                (select1 :select1-record {:id 1}))))

  (is (= nil
         (qry-> tests-db
                (select1 :select1-record {:id 2}))))

  )



(deftest select-records

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS select_records;")
    (execute cnx "CREATE TABLE select_records (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO select_records (name) VALUES ('bob'),('bill');"))

  (is (= [{:id 1 :name "bob"} {:id 2 :name "bill"}]
         (qry-> tests-db
                (select :select-records {}))))

  (is (= nil
         (qry-> tests-db
                (select :select-records {:id 3}))))

  )


(deftest select-records-with-nulls
  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS select_records_with_nulls;")
    (execute cnx "CREATE TABLE select_records_with_nulls (id serial primary key not null,
             i integer, t text, tsz timestamptz, b boolean);")
    (execute cnx "INSERT INTO select_records_with_nulls (i,t,tsz,b) VALUES (null,null,null,null);"))

  (is (= [{:id 1 :i nil :t nil :tsz nil :b nil}]
         (qry-> tests-db
                (select :select-records-with-nulls {})))))




(deftest select-arrays

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS select_arrays;")
    (execute cnx "CREATE TABLE select_arrays (id serial primary key not null, names text[], numbers integer[]);")
    (execute cnx (str "INSERT INTO select_arrays (names,numbers) VALUES "
                      "('{\"bob\", \"bill\"}','{101, 202}');")))

  (is (= {:id 1 :names ["bob" "bill"] :numbers [101 202]}
         (qry-> tests-db
                (select1 :select-arrays {:id 1}))))

  )



(deftest select-arrays-2dimensions

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS select_arrays_2dimensions;")
    (execute cnx "CREATE TABLE select_arrays_2dimensions (id serial primary key not null, letters text[][]);")
    (execute cnx (str "INSERT INTO select_arrays_2dimensions (letters) VALUES "
                      "('{{\"a\", \"b\", \"c\"}, {\"x\", \"y\", \"z\"}}');")))

  (def-query select-arrays-2dimensions-query
    {:file "taoclj/foundation/sql/select_arrays_2dimensions.sql"})

  (is (= '({:id 1 :letters (("b" "c") ("y" "z"))})
         (qry-> tests-db
                (select-arrays-2dimensions-query {}))))

  )







