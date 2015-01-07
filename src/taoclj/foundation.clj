(ns taoclj.foundation
  (:require [taoclj.foundation.dml :refer [to-sql-insert]])
  (:import [java.sql Statement Timestamp]
           [com.zaxxer.hikari HikariDataSource]))










; *** parameter mappings *********************

; TODO: figure out way to test mappings??
(defn- set-parameter-value! [^Statement statement ^Long position value]
  ; (println "parameter cls = " (class value))
  (if value
    (let [cls (class value)]
      (cond (= cls java.lang.String)  (.setString statement position value)
            (= cls java.lang.Integer) (.setInt statement position value)
            (= cls java.lang.Long)    (.setLong statement position value)
            (= cls java.time.Instant) (.setTimestamp statement position
                                                     (Timestamp/from value))
            :default
            (throw (Exception. "Parameter type not mapped!"))))))


; (class (java.sql.Timestamp/from (Instant/now)))


(defn- set-parameter-values! [^Statement statement column-names data]
  ; (println "now setting parameter values...")
  (doall
    (map (fn [col]
           (let [position (+ 1 (.indexOf column-names col))
                 value (col data)]
             (set-parameter-value! statement position value) ))
         column-names)))










; *** insert/update/delete *********************

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

      ; TODO validate resolved data is in an acceptable format!



      (set-parameter-values! statement column-names resolved-data)

        (let [rowcount       (.executeUpdate statement)
              generated-keys (.getGeneratedKeys statement)
              has-keys       (.next generated-keys)
              keys-meta      (.getMetaData generated-keys)
              generated-id   (.getObject generated-keys 1)]


          (.close statement)
          (conj rs generated-id)

  ;;         [rowcount
  ;;          (.getObject generated-keys 1) ; id column
  ;;          (.getObject generated-keys 2) ; name column
  ;;          (.getColumnName keys-meta 1)
  ;;          (.getColumnType keys-meta 1)
  ;;          (.isAutoIncrement keys-meta 1)]
  ) )) )





; *** trx->  *********************
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
              ~result-set)

            (catch Exception ~ex
              (.rollback ~cnx)
              (println ~ex)
              false)

            (finally
              (.close ~cnx)))))))





; *** result set and data transform helpers *********************

(defn validate [columns item-structure]

  ; columns must be nil or vector of keywords/strings

  ; item template structure must be a vector or map

  )

; (validate columns item-structure)


; todo unit test this
; todo move back into main namespace because it's a primary function...
(defn with-rs*
  ([data item-template]
   (with-rs* data nil item-template))

  ([data columns item-template]
    (validate columns item-template)

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



((with-rs 22 {:user-id (first rs)
              :role-id item})
 [11])

(macroexpand '(with-rs [22 33] nil {:user-id (first rs)
                                    :role-id item})
)









; *** result-rs *********************

(defn first-result [rs _]
  (first rs))

(defn nth-result [rs _ n]
  (nth rs n))







; *** datasource helpers *********************

; def-postgres, def-postgres-nopool ? create a java object with function that aquires an connection?
(def datasource
  (doto (HikariDataSource.)
        (.setDataSourceClassName "com.impossibl.postgres.jdbc.PGDataSource")
        (.setConnectionTimeout 5000)
        (.setMaximumPoolSize 3)
        (.addDataSourceProperty "Host"      "localhost")
        (.addDataSourceProperty "Port"      5432)
        (.addDataSourceProperty "Database"  "portal_db")
        (.addDataSourceProperty "User"      "portal_app")
        (.addDataSourceProperty "Password"  "dev")
        ; config.addDataSourceProperty("cachePrepStmts", "true");
        ; config.addDataSourceProperty("prepStmtCacheSize", "250");
        ; config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ; config.addDataSourceProperty("useServerPrepStmts", "true");
    ))







(trx-> datasource
       (insert :users      {:name "Bob"  :username "bob"  :password "abc123"})
       (insert :user-roles (with-rs 1 {:user-id (first rs)
                                       :role-id item})))

; perhaps we don't want the vector data structure??
(trx-> datasource
       (insert "users" {:name "Bob"  :username "bob"  :password "xxx"})
       (insert "user_roles" (with-rs role-data
                                     [:user-id :role-id]
                                     [(first rs) item])))

(defn create-user [user role]
  (trx-> datasource
         (insert :users      user)
         (insert :user-roles (with-rs role {:user-id (first rs)
                                            :role-id item}))
         (first-result)))


(create-user {:name "Bob" :username "bob" :password "abc123"}
             1)




(defn create-session [user-id started expires]
  (trx-> datasource
         (insert "sessions" {:user-id user-id
                             :started-at started
                             :expires-at expires})
         (nth-result 0)))

; nth-result
; first-result


(import '[java.time Instant Duration])

(let [started  (Instant/now)
      expires  (.plus started (Duration/ofMinutes 2))]
  (create-session 193 started expires) )






; Insert multiple syntx...
(trx-> datasource
       (insert "users" [{:name "Bob"  :username "bob"  :password "xxx"}
                        {:name "Bill" :username "bill" :password "zzz"}])
 )


; DELETE syntax
(trx-> datasource

       ; delete using maps for where
       (delete "users" {:id 1})
       (delete "users" [{:id 1} {:id 2}])

       ; delete specifing where column and values
       (delete "users" :id 1)
       (delete "users" :id [1 2])

       (delete "users" [:id :id2] [[1 2] [3 4]])

 )

; most probably we would have a repo function like this

(defn delete-product [id]
  (trx-> datasource
         (delete :resources {:product-id id})
         (delete :products  {:id id})))

=> default return row-count result set if successful?






; UPDATE syntax
(trx-> datasource

       ; update a single row with column values...
       (update "users" :id {:id 1 :name "Bob"})

       ; update multiple rows with column values...
       (update "users" :id [{:id 1 :name "Bob"} {:id 2 :name "Bill"}])

       ; update based on multiple criteria?
       (update "users" {:id 1 :account-id 22} {:name "Bob"})


       ; update multiple rows with a single value for each column...
 )


; update a user and their roles...

(defn update-user [user roles]
  (trx-> datasource
         (update :users :id user)
         (delete :user-roles :user-id (user :id))
         (insert :user-roles (with-rs roles {:user-id (user :id)
                                             :role-id item}))
         (success?)))

(update-user {:id 1 :name "Bob"}
             [2 3])



; Complex DELETE/UPDATE are pushed to sql files!































;; public void updatePrice(float price, String cofName,
;;                             String username, String password)
;;         throws SQLException{

;;         Connection con;
;;         PreparedStatement pstmt;
;;         try {
;;             con = ds.getConnection(username, password);
;;             con.setAutoCommit(false);
;;             pstmt = con.prepareStatement("UPDATE COFFEES " +
;;                         "SET PRICE = ? " +
;;                         "WHERE COF_NAME = ?");
;;             pstmt.setFloat(1, price);
;;             pstmt.setString(2, cofName);
;;             pstmt.executeUpdate();

;;             con.commit();
;;             pstmt.close();

;;         } finally {
;;             if (con != null) con.close();
;;         }
;;     }










