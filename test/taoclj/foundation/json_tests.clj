(ns taoclj.foundation.json-tests
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]
            [taoclj.foundation.execution :refer [execute]]))


(deftest read-json-columns
  (is (= '({:person {:name "bob"}})
          (qry-> tests-db
                 (execute "select '{\"name\" : \"bob\"}'::json as person;")))))


(deftest insert-json-columns
  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS insert_json;")
    (execute cnx (str "CREATE TABLE insert_json (id serial primary key not null,"
                      " person json not null);")))
  (trx-> tests-db
         (insert :insert-json {:person {:name "bob"}}))

  (is (= [{:id 1 :person {:name "bob"}}]
         (qry-> tests-db
                (execute "SELECT id, person FROM insert_json;"))  ))
  )


; waiting on driver support...
;; (deftest insert-jsonb-columns
;;   (with-open [cnx (.getConnection tests-db)]
;;     (execute cnx "DROP TABLE IF EXISTS insert_jsonb;")
;;     (execute cnx (str "CREATE TABLE insert_jsonb (id serial primary key not null,"
;;                       " person jsonb not null);")))
;;   (trx-> tests-db
;;          (insert :insert-jsonb {:person {:name "bob"}}))

;;   (is (= [{:id 1 :person {:name "bob"}}]
;;          (qry-> tests-db
;;                 (execute "SELECT id, person FROM insert_jsonb;"))  ))
;;   )






