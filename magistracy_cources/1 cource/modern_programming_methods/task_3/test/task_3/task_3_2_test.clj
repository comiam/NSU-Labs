(ns task-3.task-3-2-test
  (:require [clojure.test :refer :all]
            [task-3.common :refer :all]))

(deftest tests
              (testing "Testing p-filter-inf"
                            (is (= (filter even? (range 100)) (lazy-filter even? (range 100) 5 1)))
                            (is (= (filter even? (range 100)) (lazy-filter even? (range 100) 5 2)))
                            (is (= (filter even? (range 100)) (lazy-filter even? (range 100) 5 3)))
                            (is (= (filter even? (range 100)) (lazy-filter even? (range 100) 5 4)))))
