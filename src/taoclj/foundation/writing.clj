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



(defn- set-parameter-value! [^PreparedStatement statement ^Long position value]
  (if value
    (let [cls (class value)]

      (cond (= cls java.lang.String)  (.setString statement position value)
            (= cls java.lang.Integer) (.setInt statement position value)
            (= cls java.lang.Long)    (.setLong statement position value)
            (= cls java.lang.Boolean) (.setBoolean statement position value)
            (= cls java.time.Instant) (.setTimestamp statement position (Timestamp/from value))
            (= cls java.util.UUID)    (.setObject statement position value)

            ; convert maps to json
            (= cls clojure.lang.PersistentArrayMap)
            (.setObject statement
                        position
                        (cheshire/generate-string value))

            ; if this is a vector/list, convert to a sql array
            (sequential? value)
            (.setArray statement position (to-sql-array statement value))

            :default
            (throw (Exception. "Parameter type not mapped!"))))))




; this version handles exploded list parameters
; perhaps we just take a parsed query map?
(defn set-parameter-values [^Statement statement param-values]
  (doall
    (map-indexed
      (fn [index value]
        (set-parameter-value! statement (+ 1 index) value) )

      param-values)))

; compiled-query
; {:sql "select * from users where id=? and name in(?,?,?)"
;  :param-values (1 "bob" "joe" "bill")}

