(ns taoclj.foundation.mappings-test
  (:require [clojure.test :refer :all]
            [taoclj.foundation.mappings :refer :all]))


(deftest names-are-translated-from-db-representation
  (are [given expected] (= expected
                           (from-db-name given))
       "aaa"      :aaa
       "000"      :000
       "999"      :999
       "zzz_zzz"  :zzz-zzz
       "aa;a"     :aaa
       "AbC"      :AbC
       ))




(deftest names-are-translated-to-db-representation
  (are [given expected] (= expected
                           (to-quoted-db-name given))
       "aaa"      "\"aaa\""
       :aaa       "\"aaa\""
       "000"      "\"000\""
       :999       "\"999\""
       "zzz-zzz"  "\"zzz_zzz\""
       :zzz-zzz   "\"zzz_zzz\""
       "aa;a"     "\"aaa\"" ))


