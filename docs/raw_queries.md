# Raw Queries

WARNING execute is not safe from sql injection. Do not use with user supplied input.

```clojure
(with-open [cnx (.getConnection mydb)]
  (execute cnx "CREATE TABLE mytable (id serial primary key not null, name text);")
  (execute cnx "INSERT INTO mytable (name) VALUES('bob');"))


; Raw queries do support multiple result sets in single statement
(with-open [cnx (.getConnection mydb)]
  (execute cnx "select * from table1;select * from table2;"))

```

