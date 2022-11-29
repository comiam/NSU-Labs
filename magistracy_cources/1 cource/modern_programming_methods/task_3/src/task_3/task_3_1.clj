(ns task-3.task-3-1
  (:require [task-3.common :refer :all]))

(defn condition-for-time [x]
  (Thread/sleep 10)
  (if (even? x) true false)
  )

(println (thread-filter odd? (range 10) 3))

(time (->>
        (filter condition-for-time (range 10))
        (doall)
        ))
(time (->>
        (thread-filter condition-for-time (range 10) 1)
        (doall)
        ))
(time (->>
        (thread-filter condition-for-time (range 10) 2)
        (doall)
        ))
(time (->>
        (thread-filter condition-for-time (range 10) 4)
        (doall)
        ))
(time (->>
        (thread-filter condition-for-time (range 10) 10)
        (doall)
        ))
(shutdown-agents)
