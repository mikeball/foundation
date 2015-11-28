# Query Threading Operators

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
