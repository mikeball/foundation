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






(with-open [cnx (.getConnection tests-db)]
    (execute cnx "select 'ehlo' as msg_xxx;"))







;; (deftest insert-single-record

;;   (with-open [cnx (.getConnection testsdb)]
;;     (execute cnx "DROP TABLE IF EXISTS insert_single_record;")
;;     (execute cnx "CREATE TABLE insert_single_record (id int primary key, name text);"))


;;   (trx-> tests-db
;;          (insert :insert-single-record {:first-name "bob"}))

;;   (is (= [{:id 1 :first-name "bob"}]
;;          (qry-> tests-db
;;                 (execute "SELECT id, name FROM insert_single_record;")
;;                 (first-result))))
;;   )



;; (qry-> tests-db
;;        (execute "SELECT id, name FROM insert_single_record;")
;;        (first-result)


;;        {:iso :read-commited}
;;        )











; insert multiple records with same fields

; insert multiple records with different fields

; error-is-thrown-when-mixing-maps-and-vectors









