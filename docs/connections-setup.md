## Connection Setup


```clojure
(require '[taoclj.foundation :as pg])

(pg/def-datasource examples-db
  {:host     "localhost"
   :port     5432
   :database "examples_db"
   :username "examples_app"
   :password "password"
   :pooled   false })


```
