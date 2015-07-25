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



; qry-> returns false on errors





; ********** Select Tests ***********************


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






; ********** Templated Query Tests ***********************

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




(deftest templated-select-records

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS templated_select_records;")
    (execute cnx "CREATE TABLE templated_select_records (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO templated_select_records (name) VALUES ('bob'),('bill');"))


  (def-select templated-select-records-query
    {:file "taoclj/foundation/sql/templated_select_records.sql"})


  (is (= [{:id 1 :name "bob"}]
         (qry-> tests-db
                (templated-select-records-query {:ids [1]}))))

    (is (= [{:id 1 :name "bob"} {:id 2 :name "bill"}]
         (qry-> tests-db
                (templated-select-records-query {:ids [1 2]}))))

  (is (= nil
         (qry-> tests-db
                (templated-select-records-query {:ids [3]}))))

  )


(deftest templated-transform

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS templated_transform;")
    (execute cnx "CREATE TABLE templated_transform (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO templated_transform (name) VALUES ('bob'),('bill');"))


  (def-select templated-select-records-query
    {:file "taoclj/foundation/sql/templated_transform.sql"
     :transform #(map :name %)})

  (is (= ["bob" "bill"]
         (qry-> tests-db
                (templated-select-records-query {}))))

  )




; ********** DSL Tests ***********************


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



;; (deftest insert-parent-child-with-rs-multiple-records

;;   (with-open [cnx (.getConnection tests-db)]
;;     (execute cnx "DROP TABLE IF EXISTS parent_records; DROP TABLE IF EXISTS child_records;")
;;     (execute cnx "CREATE TABLE parent_records (id serial primary key not null, name text);")
;;     (execute cnx "CREATE TABLE child_records (parent_id int not null, related_id int not null);"))

;;   (trx-> tests-db
;;          (insert :parent-records {:name "bob"})
;;          (insert :child-records (with-rs [22 33] {:parent-id (first rs)
;;                                                   :related-id item})))

;;   (is (= [ [{:id 1 :name "bob"}] [{:parent-id 1 :related-id 22}] ]
;;          (qry-> tests-db
;;                 (execute "SELECT id, name FROM parent_records;")
;;                 (execute "SELECT parent_id, related_id FROM child_records;"))))

;;   )





(deftest delete-records

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS delete_records;")
    (execute cnx "CREATE TABLE delete_records (id serial primary key not null, name text);")
    (execute cnx "INSERT INTO delete_records (name) values ('bob'),('bill');"))

  (trx-> tests-db
         (delete :delete-records {:id 1}))

  (is (= [{:id 2 :name "bill"}]
         (qry-> tests-db
                (execute "SELECT id, name FROM delete_records;"))))

  )




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







; (run-tests *ns*)




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









