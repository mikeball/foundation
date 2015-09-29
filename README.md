# Foundation/PG

A toolkit for talking to Postgres. A clojure data access library for postgresql.

Status: Work in Progress, slightly usable.


## Rationale

I simply wanted something as easy to use as Entity Framework for postgres, minus the
ORM baggage. I wanted DateTime's mapped automatically, and underscores converted to dashes. Also I REALLY wanted a clean sytax structure for exectuting multiple queries at once, and getting a sensible result back based on outcome of query. None of the other clojure sql access libaries was quite what I desired.

Why only postgresql? Simply I've chosen to build first class support for 1 database rather than lowest common denominator support for many databases. Also a strong desire to out of the box map more complex types such as arrays & json, it really isn't possible to do that for all databases consistently.




## Goals
  Ease of use while encouraging correctness
  Embrace postgres to fullest extent possible
  Support postgresql extended datatypes (eg arrays, json, hstore, gis)


## Features
 - Automatic conversion from dash to underscore and back
 - Casing is ignored, rather than automatically converted to lower case.
 - automatic conversion from/to java.time.Instant
 - Integer and text arrays supported.
 - Supports reading multiple result sets
 - Connection Pooling built in (HikariCP)
 - Support for JSON (todo)



## Datatypes Mapped

Clojure Data Type  | Postgresql Type
------------------ | ---------------
string             | text
java.time.Instant  | timestampz
string sequence    | text array
integer sequence   | int array
map                | json
java.lang.UUID     | uuid



## Configure Connection Details
```clojure
(require '[taoclj.foundation :as pg])

(pg/def-datasource examples-db
  {:host     "localhost"
   :port     5432
   :database "examples_db"
   :username "examples_app"
   :password "password"
   :pooled   false })


```



## Query Threading Operators

The primary interaction with the database uses a threading operator like model,
with the results of each query appended to the main result set.

```clojure
qry-> ; intended for non transactional statement sets
trx-> ; intended for transactional statement sets


; on success
;     select returns rows if any are present
;     select returns nil if no rows are present
;     insert returns generated id's as sequence
;     update returns rows effected count

; on any exception
;   all statements print exception to standard out and return false


```



## Quick Intro
```clojure
; insert a record, returns generated key
(pg/trx-> examples-db
          (pg/insert :products {:name "product 1"}))
=> 1

; Select all products
(pg/qry-> examples-db
          (pg/select :products {}))

=> ({:id 1, :name "product 1"})   ; * returns a list


; Select a single record
(pg/qry-> examples-db
          (pg/select1 :products {:id 1}))

=> {:id 1, :name "product 1"}   ; * returns a single record


```

```sql
-- write more complex queries in sql template files
select p.id, p.name, c.name as category_name
  from products p
    inner join categories c on p.category_id = c.id
  where p.category_id = :category-id
```

```clojure

; define the query and reference the template file.
(def-query my-query
    {:file "path/to/my_query.sql"})

; use complex template queries
(pg/qry-> examples-db
          (my-query {:category-id 6}))

=> ({:id 1 :name "Product A" :category-name "Category 6"}
    {:id 2 :name "Product B" :category-name "Category 6"})

```





## Selecting Data with DSL
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









; templated select
def-query


; select a single result with sql template file (may be removed)
def-select1



```




## Inserting Data
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



## Templated Queries

For more complex queries, use sql template queries.





FYI: each file/query (at present) may contain only 1 statement because it's passed to the database as a JDBC prepared statement, which allows only a single sql statement per JDBC statement. A potential future feature would be to support multiple statements per file by splitting the statements and executing each seperately.

```clojure

(pg/def-query my-complex-query
  {:file "myapp/my_complex_query.sql"})



; transforms




```



## Raw Queries

WARNING execute is not safe from sql injection. Do not use with user supplied input.

```clojure
(with-open [cnx (.getConnection mydb)]
  (execute cnx "CREATE TABLE mytable (id serial primary key not null, name text);")
  (execute cnx "INSERT INTO mytable (name) VALUES('bob');"))


; Raw queries do support multiple result sets in single statement
(with-open [cnx (.getConnection mydb)]
  (execute cnx "select * from table1;select * from table2;"))

```




## Listen Notify
- TODO. we have functional code, not yet fleshed out.



## Placeholder Syntax
- :myparam
- :: is ignored (todo)
- \: escapes a colon for use in array slice syntax expressions (todo)
SELECT schedule[1\:2][1\:1] FROM sal_emp WHERE name = 'Bill'





## Explicitly Not Included

Support for other databases such as MySql, MS Sql, Oracle, etc.

Data definition sql and schema migrations. We believe that these concerns should
be handled outside of the data layer of your application and no plans to include
these into this library.





## License

Copyright © 2014 Michael Ball

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
