(ns taoclj.foundation.insert-tests
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]
            [taoclj.foundation.execution :refer [execute]]))



(deftest insert-records

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS insert_records;")
    (execute cnx "CREATE TABLE insert_records (id serial primary key not null, name text);"))

  (trx-> tests-db
         (insert :insert-records {:name "bob"}))

  (is (= [{:id 1 :name "bob"}]
         (qry-> tests-db
                (execute "SELECT id, name FROM insert_records;"))  ))

  )


(deftest insert-records-arrays

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS insert_records_arrays;")
    (execute cnx (str "CREATE TABLE insert_records_arrays (id serial primary key not null,"
                      "   names text[] not null, numbers integer[] not null);")))

  (trx-> tests-db
         (insert :insert-records-arrays
                 {:names ["bob" "bill"] :numbers [101 202]}))

  (is (= [{:id 1 :names ["bob" "bill"] :numbers [101 202]}]
         (qry-> tests-db
                (execute "SELECT id, names, numbers FROM insert_records_arrays;"))  ))

  )




(deftest insert-multiple-records

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS insert_multiple_records;")
    (execute cnx "CREATE TABLE insert_multiple_records (id serial primary key not null, name text);"))

  (trx-> tests-db
         (insert :insert-multiple-records [{:name "bob"} {:name "bill"}]))

  (is (= [{:id 1 :name "bob"} {:id 2 :name "bill"}]
         (qry-> tests-db
                (execute "SELECT id, name FROM insert_multiple_records;")) ))
  )



(deftest insert-parent-child-with-rs

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS parent_records; DROP TABLE IF EXISTS child_records;")
    (execute cnx "CREATE TABLE parent_records (id serial primary key not null, name text);")
    (execute cnx "CREATE TABLE child_records (parent_id int not null, related_id int not null);"))

  (trx-> tests-db
         (insert :parent-records {:name "bob"})
         (insert :child-records (with-rs 22 {:parent-id (first rs)
                                             :related-id item})))

  (is (= [ [{:id 1 :name "bob"}] [{:parent-id 1 :related-id 22}] ]
         (qry-> tests-db
                (execute "SELECT id, name FROM parent_records;")
                (execute "SELECT parent_id, related_id FROM child_records;"))))

  )



(deftest insert-parent-child-with-rs-multiple-records

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS parent_records; DROP TABLE IF EXISTS child_records;")
    (execute cnx "CREATE TABLE parent_records (id serial primary key not null, name text);")
    (execute cnx "CREATE TABLE child_records (parent_id int not null, related_id int not null);"))

  (trx-> tests-db
         (insert :parent-records {:name "bob"})
         (insert :child-records (with-rs [22 33] {:parent-id (first rs)
                                                  :related-id item})))

  (is (= [ [{:id 1 :name "bob"}] [{:parent-id 1 :related-id 22}
                                  {:parent-id 1 :related-id 33}] ]
         (qry-> tests-db
                (execute "SELECT id, name FROM parent_records;")
                (execute "SELECT parent_id, related_id FROM child_records;"))))

  )
