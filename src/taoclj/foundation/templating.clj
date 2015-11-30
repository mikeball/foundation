(ns taoclj.foundation.templating
  (:require [taoclj.foundation.execution :as execution]
            [taoclj.foundation.templating.loading :refer [load-template]]
            [taoclj.foundation.templating.generation :as generation]))


(defn extract-section-handlers [options]
  (->> options (map (fn [kv]
                      (if (generation/is-section-name? (first kv))
                        kv)))
       (remove nil?)
       (into {})))


(defn generate-def-query [name options]
  (let [queries      (gensym "queries")
        scanned      (gensym "scanned-query")
        rs           (gensym "rs")
        cnx          (gensym "cnx")
        params       (gensym "params")
        compiled     (gensym "compiled")
        sections     (gensym "sections")
        transform    (gensym "transform")
        result       (gensym "result")]
    `(def ~name
       (let [~queries (~load-template ~options)
             ~sections (~extract-section-handlers ~options)
             ~transform ~(if (:transform options) (:transform options) (fn [r] r))]

         (fn [~rs ~cnx ~params]
           (let [~scanned      (first ~queries)
                 ~compiled     (generation/compile-query ~scanned ~params ~sections)]
             (conj ~rs

                   (let [~result (execution/execute-prepared-query ~cnx ~compiled) ]

                     (if (nil? ~result) ~result
                       (~transform ~result))

                     ) )))
         ))))



; convert name to generate-def-query?
; compile-query should be statement delimiter(;)
;   aware and parse multiple queries if present
; convert to use multiple prepared queries

(defn generate-def-select [name options single?]
  (let [queries      (gensym "queries")
        scanned      (gensym "scanned-query")
        rs           (gensym "rs")
        cnx          (gensym "cnx")
        params       (gensym "params")
        compiled     (gensym "compiled")
        sections     (gensym "sections")
        transform    (gensym "transform")
        results1     (gensym "results1")
        results2     (gensym "results2")]
    `(def ~name
       (let [~queries (~load-template ~options)
             ~sections (~extract-section-handlers ~options)
             ~transform ~(if (:transform options) (:transform options) (fn [r] r))]

         (fn [~rs ~cnx ~params]
           (let [~scanned      (first ~queries)
                 ~compiled     (generation/compile-query ~scanned ~params ~sections)]
             (conj ~rs

                   (let [~results1 (execution/execute-prepared-query ~cnx ~compiled)
                         ~results2 ~(if single? `(first ~results1) results1) ]

                     (if (nil? ~results2) ~results2
                       (~transform ~results2))

                     ) )))

         ))))

; (generate-def-select 'select-session {} true)




