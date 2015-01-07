(ns taoclj.foundation.util
  (:import [java.sql Connection Statement]))





(defn execute
  "Returns true or false if statement was successful"
  [rs ^Connection cnx ^string sql]

  (let [statement (.createStatement cnx)
        rs        (.execute statement sql)]


    ; read in result sets??


    ; should this return select results?
    ; should we just return true/false?

    )



  )




(defn execute-query
  "returns result set returned from query"
  [rs ^Connection cnx ^string sql]

  (let [statement (.createStatement cnx)
        rs        (.execute statement sql)]


    ; read in result sets??


    ; should this return select results?
    ; should we just return true/false?

    )



  )
