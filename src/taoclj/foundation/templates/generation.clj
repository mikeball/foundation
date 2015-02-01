(ns taoclj.foundation.templates.generation
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



; (compile-query ["select * from users where id=" :id " and name in(" :names ")"]
;                {:id 1 :names ["bob" "joe" "bill"]}
;  )


; {:sql "select * from users where id=? and name in(?,?,?)"
;  :param-values (1 "bob" "joe" "bill")}





