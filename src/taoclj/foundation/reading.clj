(ns taoclj.foundation.reading
  (:require [taoclj.foundation.naming :refer [from-db-name]])
  (:import [java.sql Types]))



; *** result set readers *********************

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

                                     ; map types back from DB!
                                     (let [ct (.getColumnType rsmeta i)]

                                       (cond (= ct Types/TIMESTAMP)
                                             (.toInstant (.getTimestamp rs  i))

                                             :default
                                             (.getObject rs  i))))

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