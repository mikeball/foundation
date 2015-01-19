(ns taoclj.foundation-test
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]))



(deftest can-connect
  (with-open [cnx (.getConnection tests-db)]
    (is (-> (with-open [cnx (.getConnection tests-db)]
              (execute cnx "select version();"))
            first
            :version
            nil?
            not))))



(deftest can-select
  (with-open [cnx (.getConnection tests-db)]
    (is (= "ehlo"
           (-> (with-open [cnx (.getConnection tests-db)]
                 (execute cnx "select 'ehlo' as message;" ))
               first
               :message)))))




(deftest can-select-multiple-resultsets
  (with-open [cnx (.getConnection tests-db)]
    (is (= '(({:msg1 "ehlo1"}) ({:msg2 "ehlo2"}))
            (with-open [cnx (.getConnection tests-db)]
                 (execute cnx "select 'ehlo1' as msg1; select 'ehlo2' as msg2;" ))))))







(deftest insert-and-select-simple-record

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS insert_single_record;")
    (execute cnx "CREATE TABLE insert_single_record (id serial primary key not null, first_name text);"))

  (trx-> tests-db
         (insert :insert-single-record {:first-name "bob"}))

  (is (= [{:id 1 :first-name "bob"}]
         (qry-> tests-db
                (execute "SELECT id, first_name FROM insert_single_record;"))))

  )







; (run-tests 'taoclj.foundation-test)




;; (trx-> datasource
;;        (insert :users      {:name "Bob"  :username "bob"  :password "abc123"})
;;        (insert :user-roles (with-rs 1 {:user-id (first rs)
;;                                        :role-id item})))



; (with-open [cnx (.getConnection tests-db)]
;    (execute cnx "select 'ehlo' as msg1; select 'ehlo' as msg1;"))







;; (qry-> tests-db
;;        (execute "SELECT id, name FROM insert_single_record;")
;;        (first-result)


;;        {:iso :read-commited}
;;        )















; insert multiple records with same fields

; insert multiple records with different fields

; error-is-thrown-when-mixing-maps-and-vectors









