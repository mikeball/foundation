(ns taoclj.foundation.mappings-test
  (:require [clojure.test :refer :all]
            [taoclj.foundation :refer [trx-> qry-> execute insert select1]]
            [taoclj.foundation.mappings :refer :all]
            [taoclj.foundation.tests-config :refer [tests-db]])
  (:import [java.time Instant]
           [java.sql Types]))


(deftest names-are-translated-from-db-representation
  (are [given expected] (= expected
                           (from-db-name given))
       "aaa"      :aaa
       "000"      :000
       "999"      :999
       "zzz_zzz"  :zzz-zzz
       "aa;a"     :aaa
       "AbC"      :AbC
       ))

(deftest names-are-translated-to-db-representation
  (are [given expected] (= expected
                           (to-quoted-db-name given))
       "aaa"      "\"aaa\""
       :aaa       "\"aaa\""
       "000"      "\"000\""
       :999       "\"999\""
       "zzz-zzz"  "\"zzz_zzz\""
       :zzz-zzz   "\"zzz_zzz\""
       "aa;a"     "\"aaa\"" ))




(deftest string-types-are-round-tripped

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS string_types_are_round_tripped;")
    (execute cnx "CREATE TABLE string_types_are_round_tripped (id serial primary key not null, first_name text);"))

  (trx-> tests-db
         (insert :string-types-are-round-tripped {:first-name "bob"}))

  (let [result (qry-> tests-db
                      (select1 :string-types-are-round-tripped {:id 1}))]

    (is (= java.lang.String
           (-> result :first-name class)))

    ))




(deftest integer-types-are-round-tripped

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS integer_types_are_round_tripped;")
    (execute cnx (str "CREATE TABLE integer_types_are_round_tripped (id serial primary key not null,"
                      "ex1 smallint, ex2 integer, ex3 bigint);")))

  (trx-> tests-db
         (insert :integer-types-are-round-tripped {:ex1 111
                                                   :ex2 222
                                                   :ex3 333 }))

  (let [result (qry-> tests-db
                      (select1 :integer-types-are-round-tripped {:id 1}))]

    (is (= java.lang.Integer (-> result :id class)))
    (is (= java.lang.Short   (-> result :ex1 class)))
    (is (= java.lang.Integer (-> result :ex2 class)))
    (is (= java.lang.Long    (-> result :ex3 class)))))



(deftest datetime-types-are-round-tripped

  (with-open [cnx (.getConnection tests-db)]
    (execute cnx "DROP TABLE IF EXISTS datetimes_are_round_tripped;")
    (execute cnx (str "CREATE TABLE datetimes_are_round_tripped (id serial primary key not null,"
                      "ex1 timestamptz);")))

  (trx-> tests-db
         (insert :datetimes-are-round-tripped {:ex1 (Instant/now)}))

  (let [result (qry-> tests-db
                      (select1 :datetimes-are-round-tripped {:id 1}))]


    (is (= java.time.Instant   (-> result :ex1 class)))
    ; (is (= java.lang.Integer (-> result :ex2 class)))
    ; (is (= java.lang.Long    (-> result :ex3 class)))

    ))


; (qry-> tests-db
;        (select1 :datetimes-are-round-tripped {:id 1})
;)

; (run-tests 'taoclj.foundation.mappings-test)


; ints are converted ... to int/long/etc??
; timestampsz are converted to Instant
