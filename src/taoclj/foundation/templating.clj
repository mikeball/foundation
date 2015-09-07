(ns taoclj.foundation.templating

  (:require [taoclj.foundation.execution :as execution]
            [taoclj.foundation.templating.loading :refer [load-template]]
            [taoclj.foundation.templating.generation :refer [compile-query]]
            )

  ; (:import [java.time Instant]
  ;         [java.sql Connection Statement ])

  )



; convert name to generate-def-query?
; compile-query should be statement delimiter(;)
;   aware and parse multiple queries if present
; convert to use multiple prepared queriesjdbc batching when sending/receiving query

(defn generate-def-select [name options single?]
  (let [queries      (gensym "queries")
        scanned      (gensym "scanned-query")
        rs           (gensym "rs")
        cnx          (gensym "cnx")
        params       (gensym "params")
        compiled     (gensym "compiled")
        transform    (gensym "transform")
        results1     (gensym "results1")
        results2     (gensym "results2")]
    `(def ~name
       (let [~queries (~load-template ~options)
             ~transform ~(if (:transform options) (:transform options) (fn [r] r))]

         (fn [~rs ~cnx ~params]
           (let [~scanned      (first ~queries)
                 ~compiled     (compile-query ~scanned ~params)]
             (conj ~rs

                   (let [~results1 (execution/execute-prepared-query ~cnx ~compiled)
                         ~results2 ~(if single? `(first ~results1) results1) ]

                     (if (nil? ~results2) ~results2
                       (~transform ~results2))

                     ) )))

         ))))

; (generate-def-select 'select-session {} true)
