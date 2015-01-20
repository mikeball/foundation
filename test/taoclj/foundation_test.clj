(ns taoclj.foundation-test
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]]))


(deftest can-connect
  (is (= (with-open [cnx (.getConnection tests-db)]
           (execute cnx "select 'ehlo' as msg;"))
         '(({:msg "ehlo"})))))


(deftest qry->single-statements
  (is (= '({:msg "ehlo"})
          (qry-> tests-db
                 (execute "select 'ehlo' as msg;")))))

(deftest trx->single-statements
  (is (= '({:msg "ehlo"})
          (trx-> tests-db
                 (execute "select 'ehlo' as msg;")))))


(deftest qry->multiple-statements
  (is (= '[({:msg1 "ehlo1"}) ({:msg2 "ehlo2"})]
          (qry-> tests-db
                 (execute "select 'ehlo1' as msg1;")
                 (execute "select 'ehlo2' as msg2;")))))

(deftest trx->multiple-statements
  (is (= '[({:msg3 "ehlo3"}) ({:msg4 "ehlo4"})]
          (trx-> tests-db
                 (execute "select 'ehlo3' as msg3;")
                 (execute "select 'ehlo4' as msg4;")))))


(deftest qry->no-result
  (is (= nil             ; nil may be bad choice for result on nothing found.
         (qry-> tests-db
                (execute "select 'ehlo' where true=false;")))))

(deftest trx->no-result
  (is (= nil             ; nil may be bad choice for result on nothing found.
         (trx-> tests-db
                (execute "select 'ehlo' where true=false;")))))





(deftest multiple-results-in-single-statement-returned
  (is (= '(({:msg1 "ehlo1"}) ({:msg2 "ehlo2"}))
          (qry-> tests-db
                 (execute "select 'ehlo1' as msg1; select 'ehlo2' as msg2;")))))








;; (deftest insert-and-select-simple-record

;;   (with-open [cnx (.getConnection tests-db)]
;;     (execute cnx "DROP TABLE IF EXISTS insert_single_record;")
;;     (execute cnx "CREATE TABLE insert_single_record (id serial primary key not null, first_name text);"))

;;   (trx-> tests-db
;;          (insert :insert-single-record {:first-name "bob"}))

;;   (is (= [{:id 1 :first-name "bob"}]
;;          (qry-> tests-db
;;                 (execute "SELECT id, first_name FROM insert_single_record;"))))

;;   )







; (run-tests 'taoclj.foundation-test)




;; (trx-> datasource
;;        (insert :users      {:name "Bob"  :username "bob"  :password "abc123"})
;;        (insert :user-roles (with-rs 1 {:user-id (first rs)
;;                                        :role-id item})))











;; (qry-> tests-db
;;        (execute "SELECT id, name FROM insert_single_record;")
;;        (first-result)


;;        {:iso :read-commited}
;;        )















; insert multiple records with same fields

; insert multiple records with different fields

; error-is-thrown-when-mixing-maps-and-vectors









