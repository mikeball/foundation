(ns taoclj.foundation
  (:require [taoclj.foundation.dsl :refer [to-sql-insert
                                           to-sql-delete to-sql-update]]
            [taoclj.foundation.datasources :as datasources]
            [taoclj.foundation.execution :as execution]
            [taoclj.foundation.templating :as templating]
            [taoclj.foundation.reading :refer [read-resultsets read-resultset]]
            [taoclj.foundation.writing :refer [set-parameter-values]])
  (:import [java.time Instant]
           [java.sql Connection Statement ]))



(defn select
  ([rs cnx table-name where-equals]
   (execution/execute-select rs cnx table-name nil where-equals false))
  ([rs cnx table-name columns where-equals]
   (execution/execute-select rs cnx table-name columns where-equals false)))


(defn select1
  ([rs cnx table-name where-equals]
    (execution/execute-select rs cnx table-name nil where-equals true))
  ([rs cnx table-name columns where-equals]
    (execution/execute-select rs cnx table-name columns where-equals true)))

;(with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;  (select [] cnx :insert-single-record {:id 2} )
;)



; just make this def-query, get rid of select and select1 notion
; for templated queries?
(defmacro def-query [name options]
  (templating/generate-def-query name options))


; remove this? do we really need this?
(defmacro def-select1 [name options]
  (templating/generate-def-select name options true))


; (def-select1 select1-example
;   {:file "taoclj/sql/test-def-select1.sql"})

;; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;;   (select1-example [] cnx {:ids [1]})
;; )





; data can be
; 1 - a single map with column names/values
; 2 - a vector of maps
; 3 - a function returning any of the above

(defn insert [rs cnx table-name data]
  (let [resolved-data (cond (map? data) data
                            (sequential? data) data
                            (ifn? data) (data rs) ; perhaps validate the data returned?
                            :default    (throw
                                          (Exception. "Parameter data is not valid!")))]
    (conj rs
          (cond (map? resolved-data)
                (execution/execute-prepared-insert cnx table-name resolved-data)

                (sequential? resolved-data)
                (doall (map #(execution/execute-prepared-insert cnx table-name %)
                            resolved-data))

                :default
                (throw (Exception. "Invalid data parameter"))))))

;; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;;   (insert [] cnx :insert-multiple-records {:name "bob"}))

;; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;;   (insert [] cnx :insert-multiple-records [{:name "bob"} {:name "bill"}]))




(defn validate-with-rs-template [columns item-structure]
  ; columns must be nil or vector of keywords/strings
  ; item template structure must be a vector or map
  )
; (validate columns item-structure)



; todo: should this be moved somwhere else?
(defn- with-rs*
  ([data item-template]
   (with-rs* data nil item-template))

  ([data columns item-template]
    (validate-with-rs-template columns item-template)

    `(fn [~'rs]
       (let [~'item ~data]
         ~(if-not (sequential? data)

            `(if ~columns
               (concat [~columns] [~item-template])
               ~item-template )

            `(if ~columns
               (concat [~columns] (map (fn [~'item] ~item-template) ~data))
               (map (fn [~'item] ~item-template) ~data))

            )))))


(defmacro with-rs [& args] (apply with-rs* args))

;; ((with-rs [22 33] {:user-id (first rs)
;;               :role-id item})
;;  [11])

;; (macroexpand '(with-rs [22 33] nil {:user-id (first rs)
;;                                     :role-id item})
;; )










; DELETE syntax

(defn execute-prepared-delete [cnx table-name where-clause]
  (let [where-columns (keys where-clause)
        sql           (to-sql-delete table-name where-columns)
        statement     (.prepareStatement cnx sql) ]

    (set-parameter-values statement (map where-clause where-columns))

    (let [rowcount (.executeUpdate statement)]
      (.close statement)
      rowcount )))



(defn delete [rs cnx table-name where-clause]
  (conj rs (execute-prepared-delete cnx table-name where-clause)))

;; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;;   (delete [] cnx :insert-records {})
;; )




(defn update
  ; I don't know if I'm happy with this syntx for multiple rows needing updates...
  "Executes simple update statements.

  (trx-> datasource
         (update :users {:name \"joe\"} {:id 1}))
  "
  [rs ^Connection cnx table-name columns where]

  (let [column-names  (keys columns)
        where-columns (keys where)
        sql           (to-sql-update table-name column-names where-columns)
        param-values  (concat (map columns column-names)
                              (map where where-columns))
        statement     (.prepareStatement cnx sql) ]
    (set-parameter-values statement param-values)
    (let [rowcount (.executeUpdate statement)]
      (.close statement)
      (conj rs rowcount)) ))


; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;   (update [] cnx :update-records {:id 1 :name "joe"} {:id 2})
; )





(defn nth-result
  "Extracts the nth result from a list of foundation results"
  [rs _ n]
  (nth rs n))


(defmacro trx-> [db & statements]
  (let [cnx        (gensym "cnx")
        ex         (gensym "ex")
        result-set (gensym "result-set")
        transform (fn [statement]
                    (concat [(first statement) cnx] (rest statement)))
        full-statements (map transform statements)]

    `(let [~cnx (try (.getConnection ~db)
                     (catch Exception ~ex nil))]
       (if-not ~cnx false
          (try
            (.setAutoCommit ~cnx false)

            (let [~result-set (-> []
                                  ~@full-statements)]
              (.commit ~cnx)
              (if (= 1 (count ~result-set))
                (first ~result-set)
                ~result-set))

            (catch Exception ~ex
              (.rollback ~cnx)
              (println ~ex)
              false)

            (finally
              (.close ~cnx)))))))



; custom exception handling?
; (def-trx->
;  {:on-exception (fn [e] "do stuff with error...")})




; TODO: add try/catch?
(defmacro qry-> [db & statements]
  (let [cnx             (gensym "cnx")
        result-set      (gensym "result-set")
        transform       (fn [statement]
                          (concat [(first statement) cnx] (rest statement)))
        full-statements (map transform statements)]

    `(with-open [~cnx (.getConnection ~db)]

       (let [~result-set (-> [] ~@full-statements)]
         (if (= 1 (count ~result-set))
           (first ~result-set)
           ~result-set))

       )))

;;  (qry-> taoclj.foundation.tests-config/tests-db
;;         (execute "select 'ehlo1' as msg1;"))





(defmacro def-datasource
  "Creates a JDBC datasource"
  [dsname config]
  ; todo validate config...
  `(def ~dsname
     (if (:pooled ~config)
       (~datasources/create-pooled-datasource ~config)
       (~datasources/create-datasource ~config))))







