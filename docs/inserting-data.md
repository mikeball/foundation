# Inserting Data with DSL



```clojure

; insert single record
(pg/trx-> examples-db
          (pg/insert :products {:category-id 1 :name "Product A"}))

=> 1



; Insert multiple records in a single transaction, as independant statements
(pg/trx-> examples-db
          (pg/insert :categories {:name "Category 2"})
          (pg/insert :categories {:name "Category 3"}))

=> (2 3) ; returns generated id's as sequence



; Insert multiple rows at once, as a single statement in map format.
(pg/trx-> examples-db
          (pg/insert :categories [{:name "Category 4"}
                                  {:name "Category 5"}]))
=> (4 5)




; insert parent and product children using with-rs macro
(pg/trx-> examples-db
          (pg/insert :categories {:name "Category 6"})
          (pg/insert :products (pg/with-rs

                                 ; a sequence of values for insertion
                                 ["Product B" "Product C"]

                                 ; this is the template to use for each item upon insert
                                 ; rs   - implicitly available and is the resultset
                                 ; item - implicitly available name for each value
                                 {:category-id (first rs)
                                  :name item}

                                 )))

=> [6 (2 3)] ; returns the generated category id in first element,
             ; and sequence of product id's in second.






```
