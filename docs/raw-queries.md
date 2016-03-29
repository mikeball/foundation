# Raw Queries

WARNING execute is not safe from sql injection. Do not use with user supplied input.

```clojure

(ns foundation-examples.raw-queries
  (:require [taoclj.foundation :as pg]
            [taoclj.foundation.execution :as execution]))


(pg/def-datasource examples-db
  {:host     "localhost"
   :port     5432
   :database "examples_db"
   :username "examples_app"
   :password "password"
   :pooled   false })


(with-open [cnx (.getConnection examples-db)]
  (execution/execute cnx "CREATE TABLE people (id serial primary key not null, name text);")
  (execution/execute cnx "CREATE TABLE places (id serial primary key not null, name text);")
  (execution/execute cnx "INSERT INTO people (name) VALUES('bob');")
  (execution/execute cnx "INSERT INTO places (name) VALUES('vegas');"))

=> true


(with-open [cnx (.getConnection examples-db)]
  (execution/execute cnx "select * from people;select * from places;"))

=> [(({:id 1, :name "bob"}) ({:id 1, :name "vegas"}))]


```

