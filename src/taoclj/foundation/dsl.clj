(ns taoclj.foundation.dsl
  (:require [clojure.string :refer [join]]
            [taoclj.foundation.naming :refer [to-quoted-db-name]]))




(defn to-column-list [columns]
  (join "," (map (fn [col] (to-quoted-db-name col))
                 columns)))


(defn to-values-list [column-count]
  (str "(" (join ", " (repeat column-count "?")) ")"))


(defn to-insert-values [row-count column-count]
  (join ","
        (repeat row-count (to-values-list column-count))))


(defn to-where [columns]
  (if columns
    (str " WHERE "
         (join " AND "
               (map #(str (to-quoted-db-name %) "=?")
                    columns)))))
; (to-where [:id :id2])


(defn to-limit-offset [limit]
  (if limit (str " LIMIT " limit)))



(defn to-update-set-list [columns]
  (str " SET "
       (join ","
             (map #(str (to-quoted-db-name %) "=?")
                  columns))))
; (to-set-list [:first :last])



; to generate parameters list for IN clause
; (s/join ", " (repeat (count params) "?")


(defn to-sql-select [table-name columns where-columns limit]
  (str "SELECT "
       (if columns (to-column-list columns) "*")
       " FROM "
       (to-quoted-db-name table-name)
       (to-where where-columns)
       (to-limit-offset limit)
       ))

; (to-sql-select :users nil [:id] 1)


; (to-sql-select :insert-single-record nil nil nil)


(defn to-sql-insert [table-name columns row-count]
  (str "INSERT INTO "
       (to-quoted-db-name table-name)
       (str "(" (to-column-list columns) ")")
       "VALUES"
       (to-insert-values row-count (count columns))))


(defn to-sql-delete [table-name where-columns]
  (str "DELETE FROM "
       (to-quoted-db-name table-name)
       (to-where where-columns)))
; (to-sql-delete :users [:id :id2])


(defn to-sql-update [table-name columns where-columns]
  (str "UPDATE "
       (to-quoted-db-name table-name)
       (to-update-set-list columns)
       (to-where where-columns)))
 ; (to-sql-update :users [:first-name :last-name] [:id])













