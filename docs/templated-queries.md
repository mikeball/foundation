# Templated Queries

First write your query in a sql file and place it on your classpath. It may be any type of sql query select/insert/update/delete.


## Create a template such as /path/to/my_query.sql
```sql
select p.id, p.name, c.name as category_name
  from products p
    inner join categories c on p.category_id = c.id
  where p.category_id = :category-id
```

## Define the query and reference the template file.
```clojure
(pg/def-query my-query
    {:file "path/to/my_query.sql"})

; now use the templated query
(pg/qry-> examples-db
          (my-query {:category-id 6}))

=> ({:id 1 :name "Product A" :category-name "Category 6"}
    {:id 2 :name "Product B" :category-name "Category 6"})

```

## You can also specify a result-set transformation function
```clojure
(pg/def-query my-query2
    {:file "path/to/my_query.sql"
     :transform (fn [rows]
                   (map (fn [row] (str (:name row) " - " (:category-name row)))
                        rows)) })

; now use the templated query with row transformation
(pg/qry-> examples-db
          (my-query2 {:category-id 6}))

=> ("Product A - Category 6"
    "Product B - Category 6")
```




## Other

```clojure
; There is also a select a single result with sql template file (may be removed)
def-select1

```

FYI: each file/query (at present) may contain only 1 statement because it's passed to the database as a JDBC prepared statement, which allows only a single sql statement per JDBC statement. A potential future feature would be to support multiple statements per file by splitting the statements and executing each seperately.

