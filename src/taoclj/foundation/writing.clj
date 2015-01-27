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


(defn set-parameter-values [^Statement statement column-names data]
  ; (println "now setting parameter values...")
  (doall
    (map (fn [col]
           (let [position (+ 1 (.indexOf column-names col))
                 value (col data)]
             (set-parameter-value! statement position value) ))
         column-names)))
