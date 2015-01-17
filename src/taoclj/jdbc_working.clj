(ns taoclj.jdbc-working
  (:require [clojure.java.jdbc :as jdbc]))






;; ; for sizing of connection pool
;; ; connections = ((core_count * 2) + effective_spindle_count)
;; (.availableProcessors (Runtime/getRuntime))



;; ; *************************************







;; (def db {:subprotocol "postgresql"
;;          :subname "//127.0.0.1:5432/portal_db"
;;          :user "portal_app"
;;          :password "dev"})




;; (jdbc/insert! db "users"
;;   {:name "Joe" :username "joe" :password "xxx" }
;;   )


;; (jdbc/with-db-transaction [cnx db]
;;   (jdbc/insert! cnx "users" {:name "Joe"
;;                              :username "joe"
;;                              :password "xxx" } ))


;; (defn insert [rs cnx table data]

;;   ; handle rs as passed map from pass-on

;;   (conj rs
;;         (first (jdbc/insert! cnx table data))))


;; (insert [] db "users" {:name "Joe"
;;                        :username "joe"
;;                        :password "xxx" })


;; (defn result [rs kw]
;;   (kw (nth rs 0)))


;; (defn pass-on [rs fields]

;;   )


;; (jdbc/query db ["select * from user_roles;"])

;; (pass-on [{:id 33}] {:id "user_id"})

;; => {:rs [{:id 33}]
;;     :fields {:user-id 33}}


;; ; (pass-on :id :as :account-id) ;perhaps pass on could use a function? eg #(first %)


;; ; this is macro expanded
;; (jdbc/with-db-transaction [cnx db]
;;   (-> []
;;       (insert cnx "users" {:name "Bob" :username "joe" :password "xxx"})

;;       (insert cnx "user_roles" {:role_id 2 :user_id 34})
;;       (result :id) ))




;; ; left off here: write this as macro to translate into above.
;; (defn tran [db & statements]

;;   statements

;;   )


;; (tran db (str "test" "ing") [:a])


;; (defn create-session [data]
;;   (tran=> db (insert "sessions" data)
;;              (result :id)))




;; ; low level DML
;; ; insert => generated key or exception
;;   ; what about user_roles which has no generated key?

;; ; update => true or exception (rows effected?)
;; ; delete => true or exception




