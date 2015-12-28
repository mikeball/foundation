# Updating Data with DSL


```clojure

; a simple update
(pg/trx-> examples-db
          (pg/update :categories
                     {:name "Category A2"} ; new column values
                     {:id 1})) ; where id = 1

```

* update dsl syntax is likely to be changed.
