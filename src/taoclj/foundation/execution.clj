(ns taoclj.foundation.execution
  (:require [taoclj.foundation.reading :as reading]
            [taoclj.foundation.writing :as writing]
            [taoclj.foundation.dsl     :as dsl])
  (:import [java.sql Statement]))



(defn execute
  "Executes raw, unsafe sql. Uses jdbc statement under the hood so we can
  support multiple resultsets, but which can not be parameterized."
  ([cnx sql] (execute [] cnx sql))
  ([rs cnx sql]
   (let [statement  (.createStatement cnx)
         has-result (.execute statement sql)]
     (conj rs
           (if-not has-result ; was a result set returned?
             (do (.close statement) true) ; return some metadata??
             (reading/read-resultsets statement nil))))))


;(with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;  (execute [] cnx "select * from insert_single_record;" ) )





(defn execute-prepared-query ; execute-prepared
  "Sets parameter values and executes a jdbc prepared statement."
  [cnx compiled-query]
  ; (println "*** (:sql parsed) " (:sql compiled-query))

  (let [statement (.prepareStatement cnx (:sql compiled-query))]

    (writing/set-parameter-values statement (:param-values compiled-query))

    (if (.execute statement)
      ; .execute returns true there is a result set present, so read it
      (reading/read-resultset (.getResultSet statement) nil)

      ; .execute returns false if the first result is an update count
      (let [rowcount (.getUpdateCount statement)]
        (.close statement)
        {:row-count rowcount})

      )))


(defn execute-select [rs cnx table-name columns where-equals single?]
  (let [where-columns (keys where-equals)
        limit    (if single? 1 nil)
        compiled {:sql (dsl/to-sql-select table-name columns where-columns limit)
                  :param-values (map where-equals where-columns)}]
     (conj rs
           (let [result (execute-prepared-query cnx compiled)]
             (if single? (first result) result)))))



(defn execute-prepared-insert [cnx table-name data]
  (let [column-names (keys data)
        sql          (dsl/to-sql-insert table-name column-names 1)
        statement    (.prepareStatement cnx sql (Statement/RETURN_GENERATED_KEYS))]

    (writing/set-parameter-values statement (map data column-names))

    (let [rowcount       (.executeUpdate statement)
          generated-keys (.getGeneratedKeys statement)
          has-keys       (.next generated-keys)
          generated-id   (.getObject generated-keys 1)]

      (.close statement)
      generated-id ) ))













