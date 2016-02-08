(ns taoclj.foundation.templating.parsing-test
  (:require [clojure.test :refer :all]
            [taoclj.foundation.templating.parsing :refer :all]))



(deftest params-have-allowed-characters
  (are [given expected] (= expected
                           (param-character? given))
       \a   true
       \A   true
       \z   true
       \Z   true
       \1   true
       \9   true
       \0   true
       \-   true

       \space  false
       \;      false
       \)      false
       \=      false

    ))




(deftest sql-is-scanned
  (are [given expected] (= expected
                           (scan-sql given))

       "abc"       ["abc"]
       "a=:b"      ["a=" :b]
       "a=:b;"     ["a=" :b ";"]
       " a = :b "  [" a = " :b " "]

       "a=:b and c=:d"
       ["a=" :b " and c=" :d]

       "a=:b and c in(:d);"
       ["a=" :b " and c in(" :d ");"]

    ))



(deftest sql-single-line-comments-are-ignored
  (are [given expected]
       (= expected (scan-sql given))

       "--abc"       [""]
       "abc --xyx"  ["abc "]

       "abc --xyz \n efg"
       ["abc " " efg"]

    ))


(deftest sql-multi-line-comments-are-ignored
  (are [given expected]
       (= expected (scan-sql given))

       "abc /* xyz */ 123"
       ["abc " " 123"]

    ))
