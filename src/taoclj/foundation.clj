(ns taoclj.foundation
  (:require [taoclj.foundation.dsl :refer [to-sql-insert
                                           to-sql-delete to-sql-update]]

            [taoclj.foundation.datasources :as datasources]
            [taoclj.foundation.execution :as execution]
            [taoclj.foundation.templating :as templating]

            [taoclj.foundation.reading :refer [read-resultsets read-resultset]]
            [taoclj.foundation.writing :refer [set-parameter-values]]


            )
  (:import [java.time Instant]
           [java.sql Connection Statement ]))






; * dsl selects  *********************

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



; * templated selects  *********************

; just make this def-query, get rid of select and select1 notion for templated queries?
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








; *** insert/update/delete *********************

; data can be
; 1 - a single map with column names/values
; 2 - a vector of maps
; 4 - a function returning any of the above


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




;; (trx-> datasource
;;        (insert :users      {:name "Bob"  :username "bob"  :password "abc123"})
;;        (insert :user-roles (with-rs 1 {:user-id (first rs)
;;                                        :role-id item})))

;; ; perhaps we don't want the vector data structure??
;; (trx-> datasource
;;        (insert "users" {:name "Bob"  :username "bob"  :password "xxx"})
;;        (insert "user_roles" (with-rs role-data
;;                                      [:user-id :role-id]
;;                                      [(first rs) item])))

;; (defn create-user [user role]
;;   (trx-> datasource
;;          (insert :users      user)
;;          (insert :user-roles (with-rs role {:user-id (first rs)
;;                                             :role-id item}))
;;          (first-result)))


;; (create-user {:name "Bob" :username "bob" :password "abc123"}
;;              1)




;; (defn create-session [user-id started expires]
;;   (trx-> datasource
;;          (insert "sessions" {:user-id user-id
;;                              :started-at started
;;                              :expires-at expires})
;;          (nth-result 0)))

;; ; nth-result
;; ; first-result


;; (import '[java.time Instant Duration])

;; (let [started  (Instant/now)
;;       expires  (.plus started (Duration/ofMinutes 2))]
;;   (create-session 193 started expires) )



;; ; Insert multiple syntx... map format results in multiple statements.
;; ; could we perhaps cache across PreparedStatements?
;; (trx-> datasource
;;        (insert "users" [{:name "Bob"  :username "bob"  :password "xxx"}
;;                         {:name "Bill" :username "bill" :password "zzz"}])
;;  )

;; ; vector based format for multiple records... results in single multi-row insert statement
;; ; but batched in reasonable sizes (50 rows?) so we don't blow the parameter limits...
;; ; also how to handle lazyness? Break colums out of
;; (trx-> datasource
;;        (insert "users" [[:name  :username :password]
;;                         ["Bob"  "bob"     "xxx"]
;;                         ["Bill" "bill"    "zzz"]]) )






(defn validate-with-rs-template [columns item-structure]
  ; columns must be nil or vector of keywords/strings
  ; item template structure must be a vector or map
  )
; (validate columns item-structure)



; todo: can this be moved somwhere else?
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

;; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;;   (execute [] cnx "select * from insert_records;")
;; )

;; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;;   (execute [] cnx "insert into insert_records (name) values ('bob'),('bill');")
;; )



;; (trx-> datasource

;;        ; delete using maps for where
;;        (delete "users" {:id 1})
;;        (delete "users" [{:id 1} {:id 2}])

;;        ; delete specifing where column and values
;;        (delete "users" :id 1)
;;        (delete "users" :id [1 2])

;;        (delete "users" [:id :id2] [[1 2] [3 4]])

;;  )

;; ; most probably we would have a repo function like this

;; (defn delete-product [id]
;;   (trx-> datasource
;;          (delete :resources {:product-id id})
;;          (delete :products  {:id id})))

;; => default return row-count result set if successful?









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


; (trx-> taoclj.foundation.tests-config/tests-db
;       (update :update-records {:name "joe"} {:id 1}))



;; ; UPDATE syntax
;; (trx-> datasource

;;        ; update a single row with column values...
;;        (update "users" :id {:id 1 :name "Bob"})

;;        ; update multiple rows with column values...
;;        (update "users" :id [{:id 1 :name "Bob"} {:id 2 :name "Bill"}])

;;        ; update based on multiple criteria?
;;        (update "users" {:id 1 :account-id 22} {:name "Bob"})


;;        ; update multiple rows with a single value for each column...
;;  )


;; ; update a user and their roles...

;; (defn update-user [user roles]
;;   (trx-> datasource
;;          (update :users :id user)
;;          (delete :user-roles :user-id (user :id))
;;          (insert :user-roles (with-rs roles {:user-id (user :id)
;;                                              :role-id item}))
;;          (success?)))

;; (update-user {:id 1 :name "Bob"}
;;              [2 3])


















; How to handle additional types/mappings
; java.time.Instant       -> timestamp (with timezone??)
; jvm/guid                -> guid
; ??  -> inet (tracking ip addresses)
; ??  -> isn (product data)

; vector defaults to array in infers type? require meta declaration?
; clj/vector(ints)        -> array int
; clj/vector(strings)     -> array text

; should hstore or json be default for map?? neither??
; clj/map                 -> json/jsonb  ; clj/map defaults to json??
; clj/map+meta/:hstore    -> hstore    https://github.com/blakesmith/pghstore-clj


; ??                      -> geo point
; ??                      -> geo line
; ?? -> int4range — Range of integer
; ?? -> int8range — Range of bigint
; ?? -> numrange — Range of numeric
; ?? -> tstzrange — Range of timestamp with time zone
; ?? -> daterange — Range of date

; ?? -> Interval – time intervals, such as ‘1 hour’, ‘1 day’


; http://www.craigkerstiens.com/2014/05/07/Postgres-datatypes-the-ones-youre-not-using/
; http://www.craigkerstiens.com/2013/07/03/hstore-vs-json/


; How to specify extended types in order to properly map to postgresql?
; use with-meta



; PG Types explicitly excluded
; timestamp, money




; for sizing of connection pool
; connections = ((core_count * 2) + effective_spindle_count)
; (.availableProcessors (Runtime/getRuntime))




; low level DML
; insert => generated-key or false
  ; what about user_roles which has no generated key? true?

; update/success => rows-updated (long?)
;       /success => true+meta-rows-updated (can boolean store meta???)
;       /error   => false


; delete/success => rows-deleted (long?)
;       /error   => false

; select/no-results => nil
;       /error      => false
;       /results    => sequence

; select1/no-results => nil
;        /error      => false
;        /result     => first-result







; *** helpers *********************

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
                ~result-set)

              )

            (catch Exception ~ex
              (.rollback ~cnx)
              (println ~ex)
              false)

            (finally
              (.close ~cnx)))))))


; TODO: add try/catch
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







