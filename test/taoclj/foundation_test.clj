(ns taoclj.foundation-test
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer :all]))



; server   : pull from env, default to localhost? Put it in project.clj???

; dbname   : foundation_testdb
; username : foundation_testuser
; password : password
; hard code user/pass for test db


(deftest server-environment-variable-set
  ; confirm we have a server address env variable set
  )


(deftest can-connect-to-foundation-testdb
  ; attempt connection
  )



(deftest insert-single-record

  (trx-> connection
         (execute "DROP TABLE IF EXISTS insert_single_record;")
         (execute "CREATE TABLE insert_single_record (id int primary key, name text);"))

  (trx-> connection
         (insert :insert-single-record {:first-name "bob"}))

  (is (= [{:id 1 :first-name "bob"}]
         (qry-> connection
                (execute "SELECT id, name FROM insert_single_record;")
                (first-result))))
  )















