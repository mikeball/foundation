(ns taoclj.foundation.mappings)


(defn to-db-char [c]
  (if (= c \-) \_
    (let [n (int c)]
      (if (or (and (>= n 48) (<= n 57))
              (and (>= n 65) (<= n 122)))
        c))))


(defn to-db-name [column-name]
  (str "\"" (apply str (map to-db-char (name column-name))) "\""))
