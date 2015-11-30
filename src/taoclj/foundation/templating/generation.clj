(ns taoclj.foundation.templating.generation
  (:require [clojure.string :refer [join]]
            [taoclj.foundation.templating.parsing :as parsing]))




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


(defn is-section-name? [candidate]
  (and (keyword? candidate)
       (= "section" (namespace candidate))))

(defn call-section-handler [section-name section-handlers params]
  (let [handler (section-name section-handlers)]
    (if-not handler
      (throw (Exception. (str "Unable to locate section handler for " section-name "!")))
      (handler params))))

(defn prepare-query [scanned-query params section-handlers]
  (->> scanned-query
       (map (fn [element]
             (if-not (is-section-name? element) element
               (-> (call-section-handler element section-handlers params)
                   (parsing/scan-sql)))))
       (flatten)))


;; (prepare-query
;;  ["select * from customers where id=" :id " " :section/filters]
;;  {:id 1}
;;  {:section/filters (fn [params] "and category_id = :category-id")}
;;  )


(defn compile-query [scanned-query params section-handlers]
  (let [prepared-query (prepare-query scanned-query params section-handlers)
        param-names (filter keyword? prepared-query)
        param-info  (reduce (fn [info key]
                              (let [val (params key)]
                                (if (list-param? val)
                                  (assoc info key (count val)))))
                            {}
                            (keys params))]

    {:sql (to-placeholder-query prepared-query param-info) ; memoize eventually
     :param-values (flatten (map params param-names))}
  ))


;; (compile-query ["select * from customers where id=" :id " " :section/filters]
;;                {:id 1 :category-id 222}
;;                {:section/filters (fn [params] "and category_id=:category-id")}
;; )



;; ; a dynamic section function is defined.
;; (def-query myquery
;;   {:file "taoclj/foundation/sql/myquery.sql"
;;    :section/myorder (fn [params] "order by :name desc")})


;; ; raw query in the file looks like this
;; "select * from customers where id=:id :section/myorder"


;; ; pre-call time scan generates this
;; ["select * from customers where id=" :id " " :section/myorder]


;; ; call time calls section function with parameters and returns string
;; ;   * throws exeption if section not found
;; :section/myorder => "order by :name desc"

;; ; the string is then scanned into standard structure
;; ["order by " :name " desc"]


;; ; the dynamic scanned structure is spliced into main query structure
;; ["select * from customers where id=" :id "order by " :name " desc"]


;; ; from there we can compile as usual



;; (compile-query ["select * from customers order by " :section/myorder]
;;                {:id 1}
;; )


;; (compile-query ["select * from users where id=" :id " and name in(" :names ")"]
;;                 {:id 1 :names ["bob" "joe" "bill"]}
;; )


; {:sql "select * from users where id=? and name in(?,?,?)"
;  :param-values (1 "bob" "joe" "bill")}





