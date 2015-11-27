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
    ; (println "** top-level type: " (type top-level-list))

    ; top-level-list
    (map (fn [item]
           (if (is-array? item)
             (seq item) ; convert sub-arrays
             item))
         top-level-list)

    ))


(defn convert-from-db [rsmeta result-set index]

  (let [ct  (.getColumnType rsmeta index)
        ctn (.getColumnTypeName rsmeta index)]

    ; (println "** colume type " ct)

    (cond (= ct Types/TIMESTAMP)
          (.toInstant (.getTimestamp result-set index))

          (= ct Types/ARRAY)
          (convert-array (.getArray result-set index))

          (= ctn "json")
          (cheshire/parse-string (.getObject result-set index)
                                 (fn [k] (keyword k)))

          :default
          (.getObject result-set index)))

  )




(defn read-resultset
  ([^java.sql.ResultSet rs] (read-resultset rs nil))
  ([^java.sql.ResultSet rs result-format]

    (let [rsmeta  (.getMetaData rs)
          idxs    (range 1 (inc (.getColumnCount rsmeta)))

          columns (map from-db-name
                       (map #(.getColumnLabel rsmeta %) idxs) )

          dups    (or (apply distinct? columns)
                      (throw (Exception. "ResultSet must have unique column names")))

          get-row-vals (fn [] (map (fn [^Integer i]
                                     (convert-from-db rsmeta rs i))
                                   idxs))

          read-rows (fn readrow []
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
