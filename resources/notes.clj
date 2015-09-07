
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


; sql
select product_id, name
  from products
  where {search-clauses}










; ********************************


; this is a query example loading a single user


select user_id, account_id, password
  from users
  where user_id = :user_id

--:use account_id

--:query-as account
select name
  from accounts
  where account_id = :account_id

--:query-as roles
select r.uid
  from user_roles ur
    inner join roles r on ur.role_id = r.role_id
  where ur.user_id = :user_id



;********************************


select user_id, password, a.name as 'account_name'
  from users u
    join accounts a on u.account_id = a.id
  where user_id = :user_id

--:as roles
select r.uid
  from user_roles ur
    inner join roles r on ur.role_id = r.role_id
  where ur.user_id = :user_id



;********************************


(defm mymodel

  ; schema
  [[:responses [:id*int]
               [:first-name*text {:rules [:required [:length 1 100]]
                                  :error "First name is required."}]]
   [:partners [:id*int]
              [:name*text]]  ]

   ; relationships
   [["responses have partner" :responses.partner-id :partners.id]
    ["responses have partner :: responses.partner_id partners_id"]
    [:responses->partner :partner-id :id]

    [:responses*have-a*partner :partner-id :id]

    [:responses->partner :partner-id :id]      ]



  (qry-> mydb
         (select1 :responses {:id 1} [:id :name :partner]))

  (qry-> [mydb mymodel]
         (select :responses {:partner-id 1} [:id :name :partner-id :partner.name]))

  "select r.id, r.first_name, p.id as '_p_id', p.name as '_p_name'
      from responses r
          inner join partners p on r.partner_id = p.id
      where p.id = ?"



  => {:id 101
      :first-name "bob"
      :partner {:id 102 :name "Suncoast"}}





  [:id*int {:rules [[:range 21 130]]
              :error "Age is optional but must be at least 21"}]

  [:name#text {:rules [:required [:length 3 10]]
               :error "Name is required and must be between 3 and 10 characters."}]

  )

; if we go down the path of having a model of the database to use for query generation?

(qry-> mydb
       (select1 :response {:id 101} :partner))


; ********************************





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





