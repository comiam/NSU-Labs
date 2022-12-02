(ns task-4.task-4
  (:require [task-4.expression :refer :all])
  (:require [task-4.var-const :refer :all]))

(println (expr-to-str (z (&& (variable :lya) (variable :mya)) (variable :mya) (|| (variable :nya) (variable :rya)))))
