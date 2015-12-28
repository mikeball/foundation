# Foundation/PG

A complete toolkit for talking to Postgres. A work in progress, but usable.

## Key Features & Goals
- Simple to run query sets and transaction sets.
- SQL templating similar to yesql
- Parameters passed as PreparedStatement parameters
- A way to add dynamic query sections
- Automatic conversion of datetime datatypes
- Integer and text array support
- JSON support (partial/pending)
- Multiple result set support
- Automatic conversion from dash to underscore and back
- Keyword case unchanged, rather than automatically converted to lower case.
- Connection pooling with the excellent [HikariCP](http://brettwooldridge.github.io/HikariCP/)



## Installation

Add this to your [Leiningen](https://github.com/technomancy/leiningen) `:dependencies`

[![Clojars Project](http://clojars.org/org.taoclj/foundation/latest-version.svg)](http://clojars.org/org.taoclj/foundation)



## Quick Start
```clojure

(require '[taoclj.foundation :as pg])


;; for the sql structure see /resources/examples.sql


; define the datasource with your database details
(pg/def-datasource examples-db {:host     "localhost"
                                :port     5432
                                :database "examples_db"
                                :username "examples_app"
                                :password "password"
                                :pooled   false })



; insert a single record, returns the database generated key
(pg/trx-> examples-db
          (pg/insert :categories {:name "Category A"}))

=> 1


; select a single record, returns a map representing the row
(pg/qry-> examples-db
          (pg/select1 :categories {:id 1}))

=> {:id 1, :name "Category A"}


; insert a multiple records, returns the generated keys as sequence
(pg/trx-> examples-db
          (pg/insert :categories [{:name "Category B"}
                                  {:name "Category C"}]))
=> (2 3)


; select multiple records, returns a sequence
(pg/qry-> examples-db
          (pg/select :categories {}))

=> ({:id 1, :name "Category A"}
    {:id 2, :name "Category B"}
    {:id 3, :name "Category C"})



; insert a new category and 2 child products, returns generated keys
(pg/trx-> examples-db
          (pg/insert :categories {:name "Category D"})
          (pg/insert :products (pg/with-rs

                                 ; a sequence of values for insertion
                                 ["Product D1" "Product D2"]

                                 ; this is the template to use for each item upon insert
                                 ; rs   - implicitly available and is the resultset
                                 ; item - implicitly available name for each value
                                 {:category-id (first rs)
                                  :name item}

                                 )))

=> [4 (1 2)]

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
(pg/def-query my-query
    {:file "path_to/my_query.sql"})

; now use our template query
(pg/qry-> examples-db
          (my-query {:category-id 4}))


=> ({:id 1, :name "Product D1", :category-name "Category D"}
    {:id 2, :name "Product D2", :category-name "Category D"})


```




## Docs and Howto

- [Query Threading - running sets of queries](docs/query-threading.md)
- [Templated Queries - for more complex queries](docs/templated-queries.md)
- [Dynamic Queries - create dynamic sections](docs/dynamic-queries.md)
- [Using JSON - how to use JSON datatypes](docs/json-support.md)
- [Selects - DSL for simple select queries](docs/selecting-data.md)
- [Inserts - DSL for inserting data](docs/inserting-data.md)
- [Updates - DSL for updating data](docs/updating-data.md)
- [Deletes - DSL for deleting data](docs/deleting-data.md)
- [Raw Queries - using raw unsecure sql statements](docs/raw-queries.md)
- [Listen/Notify - push notifications from postgres](docs/listen-notify.md)




## Rationale

I simply wanted something as easy to get data into and out of postgres. I wanted DateTime's converted automatically, and underscores converted to dashes. Also I really wanted a clean sytax structure for exectuting multiple queries at once, such as adding a parent and multiple child records. I wanted sensible result back based on outcome of query. None of the other clojure sql access libaries was quite what I desired.

After much experimentation, I concluded that insert, updates and very simple select statements are better handled using a lightweight DSL. This allow transparent handling of both single items as well as sequences of items, batch inserts, cuts down on number of templated queries we need to write and means we don't have to rely on function naming conventions for return values. For any query beyond the most trivial, it's a better solution to then use a templated query.

The primary goal is ease of use while also encouraging a correct path. I want to embrace postgres to fullest extent possible, and support postgresql extended datatypes (eg arrays, json, hstore, gis)

Why support only postgresql? Simply I've chosen to build first class support for 1 database rather than lowest common denominator support for all given limited time. Also a desire to out of the box map more complex types such as arrays & json, and also to support listen/notify.





## Intentionally Not Included

Support for other databases such as MySql, MS Sql, Oracle, etc.

Data definition sql and schema migrations. We believe that these concerns should
be handled outside of the data layer of your application and no plans to include
these into this library.





## License

Copyright © 2015 Michael Ball

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
