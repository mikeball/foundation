# JSON Support

JSON datatyes are presently partially supported. Postgres JSON datatype can be both inserted and selected. JSONB may only be selected at this point, as the driver does not yet support Postgres JSONB parameters in prepared statements. See https://github.com/impossibl/pgjdbc-ng/issues/163

If using tempated queries, JSON paramters in queries only function as the entire json parameter. It is not possible to use a query parameter to set a value embedded in a json structure.

```clojure

; :options is a json column
(pg/trx-> examples-db
          (pg/insert :products {:options {:color "blue"}}))

=> 1

; json and jsonb columns are converted to clojure maps
(pg/qry-> examples-db
          (pg/select :products {}))

=> ({:options {:color "blue"}})

```
