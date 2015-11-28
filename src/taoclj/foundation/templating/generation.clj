(ns taoclj.foundation.templating.generation
  (:require [clojure.string :refer [join]]))




(defn to-placeholder-query [scanned-query params-info]
  (apply str
         (map (fn [tok]
                (if-not (keyword? tok) tok
                  (let [n (tok params-info)]
                    (if-not n "?"
                      (join "," (repeat n "?"))))))
              scanned-query)
         ))


; (to-placeholder-query ["select * from users where id=" :id]
;                       {:id 3} )




(def list-param? (some-fn list? vector? seq?))


(defn compile-query [scanned-query params]

  (let [param-names (filter keyword? scanned-query)
        param-info  (reduce (fn [info key]
                              (let [val (params key)]
                                (if (list-param? val)
                                  (assoc info key (count val)))))
                            {}
                            (keys params))]

    {:sql (to-placeholder-query scanned-query param-info) ; memoize eventually
     :param-values (flatten (map params param-names))
     ; capture/pass meta dbtypes here?
     }
  ))




;; ; a dynamic section function is defined.
;; (def-query myquery
;;   {:file "taoclj/foundation/sql/myquery.sql"
;;    :sections {"myorder" (fn [params] "order by :name desc")}})


;; ; raw query in the file looks like this
;; "select * from customers where id=:id ${myorder}"

;; ; pre-call time scan generates this
;; ["select * from customers where id=" :id :section/myorder]


;; ; call time calls section function with parameters and returns string
;; ;   * throws exeption if section not found
;; :section/myorder => "order by :name desc"

;; ; the string is then scanned into standard structure
;; ["order by " :name " desc"]


;; ; the dynamic scanned structure is spliced into main query structure
;; ["select * from customers where id=" :id "order by " :name " desc"]


;; ; from there we can compile as normal



;; (compile-query ["select * from customers order by " :section/myorder]
;;                {:id 1}
;; )


;; (compile-query ["select * from users where id=" :id " and name in(" :names ")"]
;;                 {:id 1 :names ["bob" "joe" "bill"]}
;; )


; {:sql "select * from users where id=? and name in(?,?,?)"
;  :param-values (1 "bob" "joe" "bill")}





