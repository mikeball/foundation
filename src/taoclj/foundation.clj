(ns taoclj.foundation
  (:require [taoclj.foundation.dsl :refer [to-sql-select to-sql-insert]]
            [taoclj.foundation.datasources :as datasources]
            [taoclj.foundation.reading :refer [read-resultsets read-resultset]]
            [taoclj.foundation.writing :refer [set-parameter-values]]
            [taoclj.foundation.templates.loading :refer [load-template]]
            [taoclj.foundation.templates.generation :refer [compile-query]])
  (:import [java.time Instant]
           [java.sql Statement]))




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
             (read-resultsets statement nil))))))

;(with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;  (execute [] cnx "select * from insert_single_record;" ) )




; move to foundation.reading? writing? combine reading+writing?
(defn execute-prepared-query
  "Sets parameter values and executes a jdbc prepared statement."
  [cnx compiled-query]
  ; (println "*** (:sql parsed) " (:sql compiled-query))
  ; (println "*** (:params parsed) " (:params compiled-query))

  (let [statement (.prepareStatement cnx (:sql compiled-query))]
        (set-parameter-values statement (:param-values compiled-query))
        (-> (.executeQuery statement)
            (read-resultset nil))))



; move over to foundation.dsl namespace?
(defn execute-select [rs cnx table-name columns where-equals single?]
  (let [where-columns (keys where-equals)
        limit    (if single? 1 nil)
        compiled {:sql (to-sql-select table-name columns where-columns limit)
                  :param-values (map where-equals where-columns)}]
     (conj rs
           (let [result (execute-prepared-query cnx compiled)]
             (if single? (first result) result)))))




; move over to foundation.dsl namespace??
(defn generate-def-select [name options single?]
  (let [queries      (gensym "queries")
        scanned      (gensym "scanned-query")
        rs           (gensym "rs")
        cnx          (gensym "cnx")
        params       (gensym "params")
        compiled     (gensym "compiled")
        transform    (gensym "transform")
        results1     (gensym "results1")
        results2     (gensym "results2")]
    `(def ~name
       (let [~queries (~load-template ~options)
             ~transform ~(if (:transform options) (:transform options) (fn [r] r))]
         (fn [~rs ~cnx ~params]
           (let [~scanned      (first ~queries)
                 ~compiled     (compile-query ~scanned ~params)]
             (conj ~rs

                   (let [~results1 (execute-prepared-query ~cnx ~compiled)
                         ~results2 ~(if single? `(first ~results1) results1) ]

                     (if (nil? ~results2) ~results2
                       (~transform ~results2))

                     )

                   ; assoc rest of queries
                   ))))))
  )

; (generate-def-select 'select-session {} true)




; *** selects  *********************

(defn select
  ([rs cnx table-name where-equals]
   (execute-select rs cnx table-name nil where-equals false))
  ([rs cnx table-name columns where-equals]
   (execute-select rs cnx table-name columns where-equals false)))


(defn select1
  ([rs cnx table-name where-equals]
    (execute-select rs cnx table-name nil where-equals true))
  ([rs cnx table-name columns where-equals]
    (execute-select rs cnx table-name columns where-equals true)))

;(with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;  (select [] cnx :insert-single-record {:id 2} ) )


(select :users {:id 1})



(defmacro def-select [name options]
  (generate-def-select name options false))


(defmacro def-select1 [name options]
  (generate-def-select name options true))


; (def-select1 select1-example
;   {:file "taoclj/sql/test-def-select1.sql"})

;; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;;   (select1-example [] cnx {:ids [1]})
;; )












; *** insert/update/delete *********************

; insert single map

; insert vector of maps
  ; never batch, record columns might be different
  ; separate prepared statement for each map/record

; insert vector of header/row vectors
  ; single statement
  ; eventually we might want to batch in row counts of ???
    ; for very large insert sets that is


; we will take a strategy of validating/verifying on the fly and
; throwing exceptions if any problems are encountered. We do this rather than
; attempting to validate an entire query set before proceeding. Performance/Memory!

  ; (println "data = " data)

  ; data can be
    ; 1 - a single map with column names/values
    ; 2 - a vector of maps
    ; 3 - a vector of vectors first vector is columns, following are values
    ; 4 - a function returning any of the above



(defn execute-update [cnx table-name data]

  (let [column-names (keys data)
        sql          (to-sql-insert table-name column-names 1)
        statement    (.prepareStatement cnx sql (Statement/RETURN_GENERATED_KEYS))]

    (set-parameter-values statement (map data column-names))

    (let [rowcount       (.executeUpdate statement)
          generated-keys (.getGeneratedKeys statement)
          has-keys       (.next generated-keys)
          generated-id   (.getObject generated-keys 1)]

      (.close statement)
      generated-id )

      )
  )

(defn insert [rs cnx table-name data]
  (let [resolved-data (cond (map? data) data
                            (sequential? data) data
                            (ifn? data) (data rs)
                            :default    (throw
                                          (Exception. "Passed transform not valid!")))]
    (conj rs
          (cond (map? resolved-data)
                (execute-update cnx table-name resolved-data)

                (sequential? resolved-data)
                (doall (map #(execute-update cnx table-name %)
                            resolved-data))

                :default
                (throw (Exception. "Invalid data structure"))))))


;; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;;   (insert [] cnx :insert-multiple-records {:name "bob"}))


;; (with-open [cnx (.getConnection taoclj.foundation.tests-config/tests-db)]
;;   (insert [] cnx :insert-multiple-records [{:name "bob"} {:name "bill"}]))











; *** result set and data transform helpers *********************

(defn validate-with-rs-template [columns item-structure]
  ; columns must be nil or vector of keywords/strings
  ; item template structure must be a vector or map
  )

; (validate columns item-structure)


; todo unit test this
; todo move back into main namespace because it's a primary function...
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



((with-rs [22 33] {:user-id (first rs)
              :role-id item})
 [11])

;; (macroexpand '(with-rs [22 33] nil {:user-id (first rs)
;;                                     :role-id item})
;; )







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




;; ; DELETE syntax
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







; Complex DELETE/UPDATE are pushed to sql files








; How to handle additional types/mappings
; java.time.Instant       -> timestamp (with timezone??)
; jvm/guid                -> guid
; ??  -> inet (tracking ip addresses)
; ??  -> isn (product data)

; vector defaults to array in infers type? require meta declaration?
; clj/vector(ints)        -> array int
; clj/vector(strings)     -> array nvarchar/text?

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







