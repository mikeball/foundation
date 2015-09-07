(ns taoclj.foundation.templating.loading
  (:require [clojure.java.io :refer [resource]]
            [taoclj.foundation.templating.parsing :refer [scan-sql]])
  (:import [java.io FileNotFoundException]))




(defn load-template-file [path]
  (or (some-> path resource slurp)
      (throw (FileNotFoundException. path))))

; (load-template-file "taoclj/sql/test-def-select1.sql")




(defn load-template [options]
  (let [raw (load-template-file (:file options))
        raw-queries [raw] ]
    (map scan-sql raw-queries)))



;(load-template {:file "taoclj/sql/test-def-select1.sql"}
; )







