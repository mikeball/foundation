# Dynamic Queries


Create the file /path/to/my_query_with_sections.sql

```sql
select id, name from products :section/my-where
```
Now define a query to use the file, and specify a section handler
function that returns a string to be inserted into the template.
```clojure
(def-query my-query3
    {:file "path/to/my_query_with_sections.sql"
     :section/my-where (fn [params]
                         (if (:name params) "where name=:name")) })

; Do not use raw parameter values in your section handler!
; Only use the parameter name placeholder just as you do in the other parts of the
; query, and foundation will parameterize the values for you.


; now use the query with sections
(pg/qry-> examples-db
          (my-query3 {:name "Product A"}))

=> ({:id 1 :name "Product A"})
```

