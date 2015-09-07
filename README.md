# Foundation/PG

A toolkit for talking to Postgres. A clojure data access library for postgresql.

Status: Work in Progress, slightly usable.


## Rationale

I simply wanted something as easy to use as Entity Framework for postgres, minus the
orm baggage. I wanted DateTime's mapped automatically, and underscores converted to dashes. Also I REALLY wanted a clean sytax structure for exectuting multiple queries at once, and getting a sensible result back based on outcome of query. None of the other clojure sql access libaries was what I wanted.

Why only postgresql? I've chosen to build first class support for 1 database rather than lowest common denominator support for many databases. Also a strong desire to out of the box map more complex types such as arrays & json, it really isn't possible to do that for all databases consistently.




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

The primary interaction with the database uses a thread like model,
with the results of each query appended to the main result set.

```clojure
pg/qry-> ; intended for non transactional statement sets
pg/trx-> ; intended for transactional statement sets


; success returns result set, or if not rows effected

; exceptions print to standard out and return false




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





## Selecting Data
```clojure


; simple select
(pg/qry-> examples-db
          (pg/select1 :products {:id 1}))





; multiple selects




; select a single row



; templated select
def-query


; select a single result with sql template file (may be removed)
def-select1

```




## Inserting Data
```clojure

; insert single record
(pg/trx-> examples-db
          (pg/insert :products {:name "product 1"}))


; inserting multiple records with this syntax results in single insert statement!

; insert multiple records as sequence of maps
(pg/trx-> examples-db
          (pg/insert :products [{:name "product 1"}
                                {:name "product 2"}]))

; insert multiple records as sequence of vectors
(pg/trx-> examples-db
          (pg/insert :products [[:name]
                                ["product 1"]
                                ["product 2"]]))



```



## Templated Queries

For more complex queries, use sql template queries.

FYI: each query (at present) may contain only 1 statement because it's passed to the database as a JDBC prepared
statement, which allows only a single sql statement per JDBC statement. It is my intention to eventually support
multiple statements per file by splitting the statements and executing each seperately.

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
- we have functional code, not yet fleshed out and functional



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
