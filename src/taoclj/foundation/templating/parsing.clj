(ns taoclj.foundation.templating.parsing)


(defn is-newline? [c]
  (= c \newline))


(defn param-character?
  "Checks if a character is allowed in a param."
  [c]
  (or (= c \-)
      (= c \_)
      (let [n (int c)]
          (or (and (>= n 48) (<= n 57))
              (and (>= n 65) (<= n 122))
              (= n 47) ; forward slash
              ))))
; (param-character? \/)



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
        [(keyword buf)])))



(defn scan-single-line-comment [input]
  (loop [raw input]
    (if-let [current-char (first raw)]

      (if (is-newline? current-char) ; we are changing context
        (scan-sql (rest raw))
        (recur (rest raw))

          ))))

;;  (scan-single-line-comment (seq "--abc \n asdf"))


(defn scan-multi-line-comment [input]
  (loop [raw input]
    (let [current-char (first raw)
          next-char    (second raw)]
      (if current-char
        (cond
          ; is this the begining of a nested comment block?
          ;; TODO: support multiple levels of nesting
          ;; (= current-char \/) (= next-char \*)


          ; are we encountering the end of the current multi-line-comment block?
          (and (= current-char \*) (= next-char \/))
          (scan-sql (drop 2 raw))

          :else (recur (rest raw)) )))))
;; (scan-multi-line-comment (seq "/*abc */ xyz"))



(defn scan-sql [input]
  ; could we memoize/cache this here? right level?
  (loop [raw (seq input) buf "" prior-char nil]

    (let [current-char (first raw)
          next-char (second raw)]

      (if current-char
        (cond
         ; we are changing context to read keyword
         (and (= current-char \:)
              (not= prior-char \:)
              (not= next-char \:)
              (re-matches #"[a-zA-Z]" (str next-char)))

         (concat (list buf)
                 (scan-param (rest raw)))


         ; we are changing context to read single line comment
         (and (= current-char \-) (= next-char \-))
         (concat (list buf)
                 (scan-single-line-comment (rest raw)))


         ; are we encountering a muli-line comment?
         (and (= current-char \/) (= next-char \*))
         (concat (list buf)
                 (scan-multi-line-comment (drop 2 raw)))

         :else
         (recur (rest raw)
                (str buf current-char)
                current-char))

        [buf] ))))


;; (scan-sql "abc /* xyz */ 123")

;; (scan-sql "select * from users -- where id=:id and name in(:names);
;;           select * from something;"
;;     )

;; (scan-sql "select * from customers where id=:id :section/myorder"
;; )

; (scan-sql "select '{\"name\":\"bob\"}'::json as person;")
; (scan-sql "::int")
; (scan-sql "select '123'::int as num;")
; (scan-sql "id=:a and name=:b;")
; (scan-sql "select * from users where id=:id and name in(:names);")










