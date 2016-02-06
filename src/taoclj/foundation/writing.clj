(ns taoclj.foundation.writing
  (:require [cheshire.core :as cheshire])
  (:import [java.sql Statement PreparedStatement Timestamp]))


(defn to-sql-array [statement value]
  (let [java-array (into-array value)
        value-type (class (first value))

        array-type (cond (= value-type java.lang.String)
                         "text"

                         (or (= value-type java.lang.Integer)
                             (= value-type java.lang.Long))
                         "integer"

                         :default
                         (throw (Exception. "Array type not supported!")))

        connection (.getConnection statement)]
      (.createArrayOf connection array-type java-array)))




(defprotocol SqlParam
  (set-param [v s i]))

(extend-protocol SqlParam

  java.lang.String
  (set-param [v s i]
    (.setString s i v))

  java.lang.Integer
  (set-param [v s i]
    (.setInt s i v))

  java.lang.Long
  (set-param [v s i]
    (.setLong s i v))

  java.lang.Boolean
  (set-param [v s i]
    (.setBoolean s i v))

  java.time.Instant
  (set-param [v s i]
    (.setTimestamp s i (Timestamp/from v)))

  clojure.lang.PersistentArrayMap
  (set-param [v s i]
    (.setObject s i (cheshire/generate-string v)))

  clojure.lang.PersistentVector
  (set-param [v s i]
    (.setArray s i (to-sql-array s v)))

  clojure.lang.PersistentList
  (set-param [v s i]
    (.setArray s i (to-sql-array s v)))

  java.lang.Object
  (set-param [v s i]
    (.setObject s i v))

  nil
  (set-param [v s i]
    (.setObject s i nil)))



; this version handles exploded list parameters
; perhaps we just take a parsed query map?
(defn set-parameter-values [^Statement statement param-values]
  (doall
    (map-indexed
      (fn [index value]
        (set-param value statement (+ 1 index)) )

      param-values)))

; compiled-query
; {:sql "select * from users where id=? and name in(?,?,?)"
;  :param-values (1 "bob" "joe" "bill")}

