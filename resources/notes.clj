
; update with need to clear out some sub-records by delete throws a wrench into things
; which is another query we don't necessarily want to surface in the api, but needs
; access to the prior parent record id. This wrench makes a dsl for insert/update/delete
; attractive.





; handle dynamic where?
(def-select part-search
  {:file "sql/search.sql"

   :transforms 'as-vectors

   ; name sections, dynamic?
   :sections {"search-clauses"

              ; this function should return query text and params?
              (fn [params] "part_number like '%:query'")}

              ; => "part_number like '%:query'"

              ; actual replacement of the :query with ? and parameter ordering
              ; can wait until the entire query is converted to jdbc params...

   })

(part-search {:query "cre"})



; somehow handle dynamic ordering



; (defq
; (def-query
; (defq-lazy
; Lazy to open & execute, versus open not and lazy read of results...
(def-lazyselect part-search
  ; same as others, just returns a function to invoke when you results are desired
  )







; isolation levels handled like this
; (qry-> {:ds portal-db :isolation :read-commited}
;        (my-query params))

; exception handlers like this
; (trx-> {:ds portal-db :catch (fn [ex] "custom error handling!")}
;        (user-query params))





