(ns taoclj.foundation.dml-test
  (:require [clojure.test :refer :all]
            [taoclj.foundation.dml :refer :all]))


(deftest selects-are-generated
  (are [table columns where-columns expected]
       (= expected (to-sql-select table columns where-columns))

       "users" nil ["id"]
       "SELECT * FROM \"users\" WHERE \"id\"=?"

       :users [:first-name :last-name] [:id :id2]
       "SELECT \"first_name\",\"last_name\" FROM \"users\" WHERE \"id\"=? AND \"id2\"=?"
  ))



(deftest inserts-are-generated
  (are [table columns row-count expected]
       (= expected
          (to-sql-insert table columns row-count))

       "app-users" ["first-name" "last-name"] 1
       "INSERT INTO \"app_users\"(\"first_name\",\"last_name\")VALUES(?, ?)"

       "app-users" ["first-name" "last-name"] 2
       "INSERT INTO \"app_users\"(\"first_name\",\"last_name\")VALUES(?, ?),(?, ?)"

       :app-users [:first-name :last-name] 2
       "INSERT INTO \"app_users\"(\"first_name\",\"last_name\")VALUES(?, ?),(?, ?)"
  ))


(deftest deletes-are-generated
  (are [table where-columns expected]
       (= expected
          (to-sql-delete table where-columns))

       "users" ["id"]
       "DELETE FROM \"users\" WHERE \"id\"=?"

       :users [:id :id2]
       "DELETE FROM \"users\" WHERE \"id\"=? AND \"id2\"=?"
  ))




(deftest updates-are-generated
  (are [table columns where-columns expected]
       (= expected
          (to-sql-update table columns where-columns))

       :users [:first-name :last-name] [:id]
       "UPDATE \"users\" SET \"first_name\"=?,\"last_name\"=? WHERE \"id\"=?"

       "users" ["name"] ["id" "id2"]
       "UPDATE \"users\" SET \"name\"=? WHERE \"id\"=? AND \"id2\"=?"

  ))


;; (to-sql-update :users [:name] [:id :id2]
;; )







;; (run-tests *ns*
;; )















