(ns task-2.task-2-1
  (:require [task-2.common :refer :all]))

(println "\n\nВыполняем замеры времени:")
(time (integral polynomial -50 50 0.05))
(time (integral-mem polynomial -50 50 0.05))