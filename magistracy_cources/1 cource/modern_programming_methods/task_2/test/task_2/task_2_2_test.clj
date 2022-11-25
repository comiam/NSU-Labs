(ns task-2.task-2-2-test
  (:require [clojure.test :refer :all]
            [task-2.common :refer :all]))

(defn float-compare [a b] (<= (Math/abs (float (- a b))) 1e-3))

(deftest tests
  (testing"Testing integral-seq"
    (is (float-compare 13496.333 ((inf-integral polynomial 0.1) 10)))
    (is (float-compare 14233.928 ((inf-integral polynomial 0.1) 10.09)))
    (is (float-compare 0 ((inf-integral polynomial 0.1) 0)))))

