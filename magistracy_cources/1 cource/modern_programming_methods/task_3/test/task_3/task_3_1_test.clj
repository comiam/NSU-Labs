(ns task-3.task-3-1-test
  (:require [clojure.test :refer :all]
            [task-3.common :refer :all]))

(defn condition-for-tests [x]
  (if (even? x) true false)
  )

(deftest tests
  (testing "Testing thread-filter"
    (is (=
          (filter condition-for-tests (range 100))
          (thread-filter condition-for-tests (range 100) 1)))
    (is (=
          (filter condition-for-tests (range 100))
          (thread-filter condition-for-tests (range 100) 2)))
    (is (=
          (filter condition-for-tests (range 100))
          (thread-filter condition-for-tests (range 100) 3)))
    (is (=
          (filter condition-for-tests (range 100))
          (thread-filter condition-for-tests (range 100) 10)))))
