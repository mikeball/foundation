# Foundation/PG

A clojure data access library for postgresql.


Status: Work in Progress, but usable.



## Goals
  Easy to use correctly
  Support postgresql extended datatypes (eg arrays, json, hstore, gis)


## Features
 - Automatic conversion from dash to underscore and back
 - casing is ignored, rather than automatically being lower cased.
 - automatic conversion from/to java.time.Instant
 - Supports postgresql integer and text arrays.


## Datatypes Mapped
 - UUID's



## Configure DB Connection
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
```clojure
pg/qry-> ; intended for non transactional statement sets
pg/trx-> ; intended for transactional statement sets
```



## Quick Intro
```clojure
; insert a single record, returns generated key
(pg/trx-> examples-db
          (pg/insert :products {:name "product 1"}))
=> 1

; simple select
(pg/qry-> examples-db
          (pg/select :products {}))

=> ({:id 1, :name "product 1"})
```




## TODO
```clojure






```




## Select
```clojure



; templated select
def-query


; select a single result with sql template file
def-select1

```




## Insert
```clojure

; insert single record

; insert multiple records as sequence of maps

; insert multiple records as sequence of vectors

; insert using a templated sql file
def-insert

```



## Templated Queries

FYI: each query may contain only 1 statement because it's passed to the database as a JDBC prepared
statement, which allows only a single sql statement per JDBC statement.


def-insert
def-update

```clojure
```





## Listen Notify





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
