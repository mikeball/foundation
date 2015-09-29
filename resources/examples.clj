

; ## Query Threading Operators
; pg/qry-> ; intended for non transactional statement sets
; pg/trx-> ; intended for transactional statement sets





; ## A Quick Intro
; use examples.sql for database schema

(require '[taoclj.foundation :as pg])

(pg/def-datasource examples-db
  {:host     "localhost"
   :port     5432
   :database "examples_db"
   :username "examples_app"
   :password "password"
   :pooled   false })


; insert a single category
(pg/trx-> examples-db
          (pg/insert :categories {:name "category 1"}))

=> 1 ; returns generated category id



; Insert multiple records in a single transaction, as independant statements
(pg/trx-> examples-db
          (pg/insert :categories {:name "category 2"})
          (pg/insert :categories {:name "category 3"}))

=> [2 3] ; returns generated id's as sequence


; Insert multiple rows at once, as a single statement in map format.
(pg/trx-> examples-db
          (pg/insert :categories [{:name "category 4"}
                                  {:name "category 5"}]))
=> (4 5)

; TODO - Insert multiple rows at once, as a single statement in vector format.
#_(pg/trx-> examples-db
          (pg/insert :categories [[:name]
                                  ["category 6"]
                                  ["category 7"]]))



; select all categories
(pg/qry-> examples-db
          (pg/select :categories {}))

=> ({:id 1 :name "category 1"}
    {:id 2 :name "category 2"}
    {:id 3 :name "category 3"}
    {:id 4 :name "category 4"}
    {:id 5 :name "category 5"}) ; returns list of all matching results



; select a single category
(pg/qry-> examples-db
          (pg/select1 :categories {:id 2}))

=> {:id 2 :name "category 2"} ; not it's not wrapped in sequence.



; query with no results
(pg/qry-> examples-db
          (pg/select :products {}))

=> nil






; insert parent category and product children at once.
(pg/trx-> examples-db
          (pg/insert :categories {:name "Category 6"})
          (pg/insert :products (pg/with-rs

                                 ; a sequence of values for insertion
                                 ["Product A" "Product B"]

                                 ; this is the template to use for each item upon insert
                                 ; rs   - implicitly available and is the resultset
                                 ; item - implicitly available name for each value
                                 {:category-id (first rs)
                                  :name item}

                                 )

                      ))
=> [6 (1 2)] ; returns the generated category id in first element,
             ; and sequence of product id's in second.



; select all products in our newly created category
(pg/qry-> examples-db
          (pg/select :products {:category-id 6}))

=> ({:id 1 :category-id 6 :name "Product A"}
    {:id 2 :category-id 6 :name "Product B"})



; insert a second category and associated product
(pg/tran-> examples-db
           (pg/insert :categories {:name "category 2"})
           (pg/insert :products (with-rs {:category-id %
                                          :name "product 3"})))
=> (2 3) ; returns generated category and product id



; select products in category 6
(pg/qry-> examples-db
          (pg/select :products {:category-id 6}))

=> ({:id 1, :category-id 6, :name "Product A"}
    {:id 2, :category-id 6, :name "Product B"})


; select a specific category and all associated products
(pg/qry-> examples-db
          (pg/select1 :categories {:id 6})
          (pg/select :products {:category-id 6}))

=> [{:id 6, :name "Category 6"}
    ({:id 1, :category-id 6, :name "Product A"} {:id 2, :category-id 6, :name "Product B"})]







; ## Configure DB connection
(require '[taoclj.foundation :as pg])

(pg/def-datasource examples-db
  {:host     "localhost"
   :port     5432
   :database "examples_db"
   :username "examples_app"
   :password "password"
   :pooled   false })





; ## Insert Syntax
; Any data manipulation statements should be wrapped in trx->

; insert a single record, returns generated key
(pg/trx-> examples-db
          (pg/insert :categories {:cat-name "category 1"}))
=> 1


; insert multiple records, returns list of generated keys
; when passed a sequence, pg/insert builds a single insert statement
(pg/trx-> examples-db
          (pg/insert :categories [{:name "product 2"}
                                {:name "product 3"}]))
=> (2 3)


; insert multiple records, returns list of generated keys
; this executes 2 independant insert statements
(pg/trx-> examples-db
          (pg/insert :products {:name "product 4"})
          (pg/insert :products {:name "product 5"}))
=> (4 5)













; perform multiple queries, returns list of result sets
(pg/trx-> examples-db
          (pg/insert :products {:name "product 4"})
          (pg/select :products {:id 4}))

=> [4 ({:id 4, :name "product 4"})]


; simple select, returns list of products
(pg/qry-> examples-db
          (pg/select :products {}))
=> ({:id 1, :name "product 1"}
    {:id 2, :name "product 2"}
    {:id 3, :name "product 3"})




; select a single record, returns a map back
(pg/qry-> examples-db
          (pg/select1 :products {:id 2}))
=> {:id 2, :name "product 2"}


; query within within a transaction that fails returns false
(pg/trx-> examples-db
          (pg/insert :badtable {:name "xxx"}))















;; (def tests-db
;;   (doto (PGDataSource.)
;;         (.setHost     "localhost") ; todo move into
;;         (.setPort     5432)
;;         (.setDatabase "foundation_tests")
;;         (.setUser     "foundation_tests_user")
;;         (.setPassword "password")))



;; ; def-postgres, def-postgres-nopool ? create a java object with function that aquires an connection?
;; (def datasource
;;   (doto (HikariDataSource.)
;;         (.setDataSourceClassName "com.impossibl.postgres.jdbc.PGDataSource")
;;         (.setConnectionTimeout 5000)
;;         (.setMaximumPoolSize 3)
;;         (.addDataSourceProperty "Host"      "localhost")
;;         (.addDataSourceProperty "Port"      5432)
;;         (.addDataSourceProperty "Database"  "portal_db")
;;         (.addDataSourceProperty "User"      "portal_app")
;;         (.addDataSourceProperty "Password"  "dev")
;;         ; config.addDataSourceProperty("cachePrepStmts", "true");
;;         ; config.addDataSourceProperty("prepStmtCacheSize", "250");
;;         ; config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
;;         ; config.addDataSourceProperty("useServerPrepStmts", "true");
;;     ))
