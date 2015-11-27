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
              (concat (list (keyword buf))
                      (scan-sql raw))
              :else
              (recur (rest raw)
                     (str buf current-char)))
        [(keyword buf)]
  )))


(defn scan-sql [input]

  ; could we memoize/cache this here? right level?

  (loop [raw (seq input) buf "" prior-char nil]

    (let [current-char (first raw)
          next-char (second raw)]

    (if current-char

      (cond (and (= current-char \:)
                 (not= prior-char \:)
                 (not= next-char \:)
                 (re-matches #"[a-zA-Z]" (str next-char))) ; we are changing context

            (concat (list buf)
                    (scan-param (rest raw)))
            :else
            (recur (rest raw)
                   (str buf current-char)
                   current-char))

      [buf] ))))



; (scan-sql "select '{\"name\":\"bob\"}'::json as person;")
; (scan-sql "::int")
; (scan-sql "select '123'::int as num;")
; (scan-sql "id=:a and name=:b;")
; (scan-sql "select * from users where id=:id and name in(:names);")










