(ns taoclj.foundation.resolution)




; handle plain map data

; always return vector formatted data?

(defn resolve-data [data]

  (cond (map? data) data
        ; todo handle vector data
                            (ifn? data) (data rs)
                            :default    (throw
                                          (Exception. "Passed transform not valid!")))



  )




(resolve-data {:name "bob"})
=> [[:name] ["bob"]]

(resolve-data [{:name "bob"} {:name "bill"}])
=> [[:name] ["bob"] ["bill"]]
