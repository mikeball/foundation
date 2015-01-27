(ns taoclj.foundation
  (:require [taoclj.foundation.dml :refer [to-sql-select to-sql-insert]]
            [taoclj.foundation.datasources :as datasources]
            [taoclj.foundation.reading :refer [read-resultsets read-resultset]]
            [taoclj.foundation.writing :refer [set-parameter-values]] )
  (:import [java.time Instant]
           [java.sql Statement] ))




(defn execute
  "Executes raw, unsafe sql. Uses statement under the hood so we can
  support multiple resultsets, but which can not be parameterized."
  ([cnx sql] (execute [] cnx sql))
  ([rs cnx sql]
   (let [statement  (.createStatement cnx)
         has-result (.execute statement sql)]
     (conj rs
           (if-not has-result ; was a result set returned?
             (do (.close statement) true) ; return some metadata??
             (read-resultsets statement nil))))))

; (qry-> taoclj.foundation.tests-config/tests-db
;       (execute "select 'ehlo' as msg1;"))











; *** simple select dsl  *********************

; should select1 throw exception if more than 1 record is found? yes?
; should select1 throw exception of no records are found? return nil?
(defn select1
  ([rs cnx table-name where-equals]
   (select1 rs cnx table-name nil where-equals))

  ([rs cnx table-name columns where-equals]
   (let [where-columns (keys where-equals)
          sql (to-sql-select table-name columns where-columns 1)
          statement (.prepareStatement cnx sql)]
      (set-parameter-values statement where-columns where-equals)
      (conj rs (-> (.executeQuery statement)
                   (read-resultset nil)
                   first)))))









(def-select1 select-session
  {:file ""}

  ; prep sql file
  ; <-

  (fn [params]
    ; question - prepared statement or statement?

    ; convert params to array


    )
  )


(defn load-session [id]
  (qry-> portal-db
         (select-session {:id id})))






















(defn select
  ([rs cnx table-name where-equals]
   (select rs cnx table-name nil where-equals))

  ([rs cnx table-name columns where-equals]
    (let [where-columns (keys where-equals)
          sql (to-sql-select table-name columns where-columns nil)
          statement (.prepareStatement cnx sql)]
      (set-parameter-values statement where-columns where-equals)
      (conj rs (read-resultset (.executeQuery statement) nil)))))


;; (qry-> taoclj.foundation.tests-config/tests-db
;;        (select1 :insert-single-record {:id 1}))








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

(defn insert [rs cnx table-name data]
  ; (println "data = " data)

  ; data can be
    ; 1 - a single map with column names/values
    ; 2 - a vector of maps
    ; 3 - a vector of vectors first vector is columns, following are values
    ; 4 - a function returning any of the above

  (let [resolved-data (cond (map? data) data
                            ; todo handle vector data
                            (ifn? data) (data rs)
                            :default    (throw
                                          (Exception. "Passed transform not valid!")))]

    ; handle multiple items to insert


    (let [column-names (keys resolved-data)
          sql       (to-sql-insert table-name column-names 1)
          statement (.prepareStatement cnx sql (Statement/RETURN_GENERATED_KEYS))]


      (set-parameter-values statement column-names resolved-data)

        (let [rowcount       (.executeUpdate statement)
              generated-keys (.getGeneratedKeys statement)
              has-keys       (.next generated-keys)
              keys-meta      (.getMetaData generated-keys)
              generated-id   (.getObject generated-keys 1)]


          (.close statement)
          (conj rs generated-id)

      ))))












;; ; *** result set and data transform helpers *********************

;; (defn validate-with-rs-template [columns item-structure]
;;   ; columns must be nil or vector of keywords/strings
;;   ; item template structure must be a vector or map
;;   )

;; ; (validate columns item-structure)


;; ; todo unit test this
;; ; todo move back into main namespace because it's a primary function...
;; (defn- with-rs*
;;   ([data item-template]
;;    (with-rs* data nil item-template))

;;   ([data columns item-template]
;;     (validate columns item-template)

;;     `(fn [~'rs]
;;        (let [~'item ~data]
;;          ~(if-not (sequential? data)

;;             `(if ~columns
;;                (concat [~columns] [~item-template])
;;                ~item-template )

;;             `(if ~columns
;;                (concat [~columns] (map (fn [~'item] ~item-template) ~data))
;;                (map (fn [~'item] ~item-template) ~data))

;;             )))))


;; (defmacro with-rs [& args] (apply with-rs* args))



;; ((with-rs 22 {:user-id (first rs)
;;               :role-id item})
;;  [11])

;; (macroexpand '(with-rs [22 33] nil {:user-id (first rs)
;;                                     :role-id item})
;; )















;; ; https://github.com/clojure/clojure/blob/clojure-1.6.0/src/clj/clojure/core.clj#L5244
;; ; https://github.com/clojure/java.jdbc/blob/master/src/main/clojure/clojure/java/jdbc.clj#L362


; http://clojure-doc.org/articles/ecosystem/java_jdbc/using_sql.html
; http://docs.oracle.com/javase/tutorial/jdbc/basics/sqldatasources.html
; http://docs.oracle.com/javase/8/docs/api/java/sql/Connection.html#prepareStatement-java.lang.String-int-





;; (execute datasource "create table ...")
;; => true

;; (execute datasource "select * from users;")
;; => [{:name "bob"} {:name "bill"}]


;; (execute [:a] datasource "create table ...")
;; => [:a true]

;; (execute [:a] datasource "select * from users;")
;; => [:a [{:name "bob"} {:name "bill"}]]







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







