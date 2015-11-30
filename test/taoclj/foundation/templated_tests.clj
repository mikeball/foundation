(ns taoclj.foundation.templated-tests
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]
            [taoclj.foundation.execution :refer [execute]]))



(deftest templated-select1-record

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS templated_select1_record;")
    (execute cnx "CREATE TABLE templated_select1_record (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO templated_select1_record (name) VALUES ('bob');"))


  (def-select1 templated-select1-record-query
    {:file "taoclj/foundation/sql/templated_select1_record.sql"})


  (is (= {:id 1 :name "bob"}
         (qry-> tests-db
                (templated-select1-record-query {:id 1}))))

  (is (= nil
         (qry-> tests-db
                (templated-select1-record-query {:id 2}))))

  )



(deftest templated-query-selects-return-expected

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS templated_query_selects;")
    (execute cnx "CREATE TABLE templated_query_selects (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO templated_query_selects (name) VALUES ('bob'),('bill');"))


  (def-query templated-select-query
    {:file "taoclj/foundation/sql/templated_select_query.sql"})


  (is (= [{:id 1 :name "bob"}]
         (qry-> tests-db
                (templated-select-query {:ids [1]}))))

  (is (= [{:id 1 :name "bob"} {:id 2 :name "bill"}]
         (qry-> tests-db
                (templated-select-query {:ids [1 2]}))))

  (is (= nil
         (qry-> tests-db
                (templated-select-query {:ids [3]}))))

  )


(deftest templated-query-inserts-return-expected

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS templated_query_inserts;")
    (execute cnx "CREATE TABLE templated_query_inserts (id serial primary key not null, name text);"))

  (def-query templated-insert-query
    {:file "taoclj/foundation/sql/templated_insert_query.sql"})

  (is (= {:row-count 2}
         (qry-> tests-db
                (templated-insert-query {}))))

)


(deftest templated-transform-returns

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS templated_transform;")
    (execute cnx "CREATE TABLE templated_transform (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO templated_transform (name) VALUES ('bob'),('bill');"))


  (def-query templated-transform
    {:file "taoclj/foundation/sql/templated_transform.sql"
     :transform #(map :name %)})

  (is (= ["bob" "bill"]
         (qry-> tests-db
                (templated-transform {}))))

  )



(deftest templated-sections-are-handled

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS templated_sections;")
    (execute cnx "CREATE TABLE templated_sections (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO templated_sections (name) VALUES ('bob'),('bill');"))

  (def-query templated-sections-query
    {:file "taoclj/foundation/sql/templated_sections.sql"
     :section/filters (fn [params]
                        (if (:name params) "name=:name"))})

  (is (= '({:id 1 :name "bob"})
          (qry-> tests-db
                 (templated-sections-query {:name "bob"}))))

  )




