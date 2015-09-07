

; ## Query Threading Operators
pg/qry-> ; intended for non transactional statement sets
pg/trx-> ; intended for transactional statement sets





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


; insert a category
(pg/tran-> examples-db
           (pg/insert :categories {:name "category 1"}))

=> 1 ; returns generated category id


; select the category we just inserted
(pg/qry-> examples-db
          (pg/select1 :categories {:id 1}))

=> {:id 1, :name "category 1"}



; insert 2 products into category 1
(pg/tran-> examples-db
           (pg/insert :products [{:category-id 1 :name "product 1"}
                                 {:category-id 1 :name "product 2"}]))
=> (1 2) ; returns the generated product id's



; select all products
(pg/qry-> examples-db
          (pg/select :products {:category-id 1}))

=> ({:id 1 :category-id 1 :name "product 1"}
    {:id 2 :category-id 1 :name "product 2"})



; insert a second category and associated product
(pg/tran-> examples-db
           (pg/insert :categories {:name "category 2"})
           (pg/insert :products (with-rs {:category-id %
                                          :name "product 3"})))
=> (2 3) ; returns generated category and product id



; select a list products with category name













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
