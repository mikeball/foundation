# Foundation/PG

A toolkit for talking to Postgres. A clojure data access library for postgresql.

Status: Work in Progress, slightly usable.


## Features
 - Automatic conversion from dash to underscore and back
 - Casing is ignored, rather than automatically converted to lower case.
 - automatic conversion from/to java.time.Instant
 - Integer and text arrays supported.
 - Supports reading multiple result sets
 - Connection Pooling built in (HikariCP)
 - Support for JSON (todo)



## Docs and Howto

[Selecting data](docs/selecting_data.md)


inserting data
updating data

templated queries




## Rationale

I simply wanted something as easy to use as Entity Framework for postgres, minus the ORM baggage. I wanted DateTime's handled automatically, and underscores converted to dashes. Also I really wanted a clean sytax structure for exectuting multiple queries at once, and getting a sensible result back based on outcome of query. None of the other clojure sql access libaries was quite what I desired.

We also believe that insert, updates and very simple select statements are better handled using a datastructure DSL. This allow transparent handling of both single paramter items as well as sequences of items, cuts down on number of templated queries to write and not have to rely on function naming conventions. For any select query beyond the trivial, use a templated query.


Why support only postgresql? Simply I've chosen to build first class support for 1 database rather than lowest common denominator support for all. A desire to out of the box map more complex types such as arrays & json, and also to support listen/notify.




## Goals
  Ease of use while encouraging correct path.
  Embrace postgres to fullest extent possible.
  Support postgresql extended datatypes (eg arrays, json, hstore, gis)





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



## Quick Start
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
