(ns taoclj.foundation.reading
  (:require [taoclj.foundation.naming :refer [from-db-name]]
            [cheshire.core :as cheshire])
  (:import [java.sql Types]))


; *** result set readers *********************



; array conversions
(def type-int-array (Class/forName "[I"))
(def type-string-array (Class/forName "[Ljava.lang.String;"))


; left off need to add support for all array types
; also get type only once, use let block
(defn is-array? [item]
  (or (= type-string-array (type item))))


(defn convert-array [array]
  (let [top-level-list (seq (.getArray array))]
    ; top-level-list
    (map (fn [item]
           (if (is-array? item)
             (seq item) ; convert sub-arrays
             item))
         top-level-list) ))


(defn convert-from-db [rsmeta result-set index]
  (let [ct    (.getColumnType rsmeta index)
        ctn   (.getColumnTypeName rsmeta index)]

    (cond (= ct Types/TIMESTAMP)
          (if-let [ts (.getTimestamp result-set index)]
            (.toInstant ts))

          (= ct Types/ARRAY)
          (if-let [a (.getArray result-set index)]
            (convert-array a))

          (= ctn "json")
          (if-let [json (.getObject result-set index)]
            (cheshire/parse-string json (fn [k] (keyword k))))

          :default
          (.getObject result-set index))))


(defn read-resultset
  ([^java.sql.ResultSet rs] (read-resultset rs nil))
  ([^java.sql.ResultSet rs result-format]

   (println "now reading result set...")
    (let [rsmeta  (.getMetaData rs)
          idxs    (range 1 (inc (.getColumnCount rsmeta)))

          columns (map from-db-name
                       (map #(.getColumnLabel rsmeta %) idxs) )

          dups    (or (apply distinct? columns)
                      (throw (Exception. "ResultSet must have unique column names")))

          ; break out function for perf
          get-row-vals (fn [] (map (fn [^Integer i]
                                     (convert-from-db rsmeta rs i))
                                   idxs))

          ; break out function for perf
          read-rows (fn readrow []
                        (println "now reading row...")
                        (when (.next rs)
                          (if (= result-format :rows)
                            (cons (vec (get-row-vals)) (readrow))
                            (cons (zipmap columns (get-row-vals)) (readrow)))))]


      (if (= result-format :rows)
        (cons (vec columns) (read-rows))
        (read-rows)) )))


(defn read-resultsets [^java.sql.Statement statement result-format]
  (let [read-sets (fn readrs []
                    (let [rs (.getResultSet statement)]
                      (cons (read-resultset rs result-format)
                            (if (.getMoreResults statement)
                              (readrs)))))
        results   (read-sets)]
    (if (= 1 (count results))
      (first results)
      results )))
