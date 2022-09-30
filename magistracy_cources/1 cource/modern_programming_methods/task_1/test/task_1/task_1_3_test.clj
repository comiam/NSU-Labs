(ns task-1.task-1-3-test
  (:require [clojure.test :refer :all]
            [task-1.task-1-3 :refer :all]))

(deftest test-map
  (testing "Test my-map"
    (is (= `(2 4 6 8) (my-map inc `(1 3 5 7))))))

(deftest test-filter
  (testing "Test my-filter"
    (is (= `(2 4) (my-filter even? `(1 2 3 4 5))))))
