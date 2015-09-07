(ns taoclj.working
  (:import [com.impossibl.postgres.jdbc PGDataSource])
  (:require [taoclj.foundation :as pg]) )

(def working-db
  (doto (PGDataSource.)
        (.setHost     "localhost") ; todo move into
        (.setPort     5432)
        (.setDatabase "foundation_working")
        (.setUser     "foundation_working_user")
        (.setPassword "password")))







(defn load-response-and-partner [id]
  (qry-> partnerforms-db
         (select1 :responses {:id 101}))



(pg/qry-> working-db
          (pg/select1 :responses {:id 3})
)


(pg/trx-> working-db
          (pg/insert :responses {:solutions ["terminal3" "moto3"]
                                 :times [301 302]}))

(pg/trx-> working-db
          (pg/update :responses {:solutions ["xterminal3" "xmoto3"]
                                 :times [401 402]}
                                {:id 3}))




;; CREATE TABLE responses (
;;   id              serial primary key not null,
;;   solutions       text[] not null,
;;   times           integer[] not null
;; );

;; insert into responses (solutions, times)
;;   values ('{"terminal2", "moto2"}',
;;           '{101, 202, 303}');

;; CREATE USER foundation_working_user WITH PASSWORD 'password';
;; GRANT SELECT,INSERT,UPDATE,DELETE  ON  responses         TO foundation_working_user;
;; GRANT SELECT,USAGE                 ON  responses_id_seq  TO foundation_working_user;
