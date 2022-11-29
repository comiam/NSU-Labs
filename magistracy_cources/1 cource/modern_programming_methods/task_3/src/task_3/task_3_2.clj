(ns task-3.task-3-2
  (:require [task-3.common :refer :all]))


(defn condition_for_time [x]
  (Thread/sleep 10)
  (if (even? x) true false)
  )

(defn my-lazy-filter [cond? col n] (lazy-filter cond? col 10 n))

(println (doall (my-lazy-filter even? (range 10) 3)))

(time (->>
        (filter condition_for_time (range 10))
        (doall)
        ))
(time (->>
        (my-lazy-filter condition_for_time (range 10) 1)
        (doall)
        ))
(time (->>
        (my-lazy-filter condition_for_time (range 10) 2)
        (doall)
        ))
(time (->>
        (my-lazy-filter condition_for_time (range 10) 10)
        (doall)
        ))

(shutdown-agents)
