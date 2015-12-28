# Deleting Data with DSL


```clojure

; a simple delete
(pg/trx-> examples-db
          (delete :categories {:id 1}))

```

