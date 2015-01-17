(ns taoclj.foundation.mappings)


(defn- convert-char [c]
  (let [n (int c)]
      (if (or (and (>= n 48) (<= n 57))
              (and (>= n 65) (<= n 122)))
        c)))

(defn- from-db-char [c]
  (if (= c \_) \-
    (convert-char c)))


(defn- to-db-char [c]
  (if (= c \-) \_
    (convert-char c)))


(defn- to-db-name [column-name]
  (apply str (map to-db-char (name column-name))))


(defn from-db-name [^String column-name]
  (keyword (apply str (map from-db-char column-name))))


(defn to-quoted-db-name [column-name]
  (str "\"" (to-db-name column-name) "\""))
