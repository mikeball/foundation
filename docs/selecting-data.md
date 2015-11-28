# Selecting Data with DSL

```clojure

; select all categories
(pg/qry-> examples-db
          (pg/select :categories {}))

=> ({:id 1 :name "category 1"}
    {:id 2 :name "category 2"}) ; returns list of all matching results


; select all records with a where clause
(pg/qry-> examples-db
          (pg/select :products {:category-id 6}))

=> ({:id 1, :category-id 6, :name "Product A"}
    {:id 2, :category-id 6, :name "Product B"})



; select a single row
(pg/qry-> examples-db
          (pg/select1 :products {:id 1}))

=> {:id 1, :category-id 6, :name "Product A"}



; issue multiple select statements at once
(pg/qry-> examples-db
          (pg/select1 :categories {:id 6})
          (pg/select :products {:category-id 6}))

=> [{:id 6, :name "Category 6"}
    ({:id 1, :category-id 6, :name "Product A"}
     {:id 2, :category-id 6, :name "Product B"})]


```
