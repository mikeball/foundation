(ns taoclj.foundation.writing
  (:import [java.sql Statement Timestamp]))


(defn- set-parameter-value! [^Statement statement ^Long position value]
  ; (println "parameter cls = " (class value))
  (if value
    (let [cls (class value)]
      (cond (= cls java.lang.String)  (.setString statement position value)
            (= cls java.lang.Integer) (.setInt statement position value)
            (= cls java.lang.Long)    (.setLong statement position value)
            (= cls java.time.Instant) (.setTimestamp statement position
                                                     (Timestamp/from value))
            :default
            (throw (Exception. "Parameter type not mapped!"))))))


; (class (java.sql.Timestamp/from (Instant/now)))



; this version handles exploded list parameters
; perhaps we just take a parsed query map?
(defn set-parameter-values [^Statement statement param-values]
  (doall
    (map-indexed
      (fn [index value]
        (set-parameter-value! statement (+ 1 index) value) )

      param-values)))

; compiled-query
; {:sql "select * from users where id=? and name in(?,?,?)"
;  :param-values (1 "bob" "joe" "bill")}

