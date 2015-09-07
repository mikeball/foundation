(ns taoclj.foundation.templating.parsing)




(defn param-character?
  "Checks if a character is allowed in a param."
  [c]
  (or (= c \-)
      (= c \_)
      (let [n (int c)]
          (or (and (>= n 48) (<= n 57))
              (and (>= n 65) (<= n 122))
              ; numbers

              ))))

; (param-character? \a)



(declare scan-sql)


(defn scan-param [input]
  (loop [raw input buf ""]
    (if-let [current-char (first raw)]
        (cond (not (param-character? current-char)) ; we are changing context
              (concat [(keyword buf)]
                      (scan-sql raw))
              :else
              (recur (rest raw)
                     (str buf current-char)))
        [(keyword buf)]
  )))


(defn scan-sql [input]

  ; could we memoize/cache this here? right level?

  (loop [raw (seq input) buf ""]
    (if-let [current-char (first raw)]
      (cond (= current-char \:) ; we are changing context
            (concat [buf]
                    (scan-param (rest raw)))
            :else
            (recur (rest raw)
                   (str buf current-char)))

      [buf] )))


; (scan-sql "id=:a and name=:b;")
; (scan-sql "select * from users where id=:id and name in(:names);")

















