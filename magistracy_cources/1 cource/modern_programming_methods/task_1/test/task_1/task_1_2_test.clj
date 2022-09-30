(ns task-1.task-1-2-test
  (:require [clojure.test :refer :all]
            [task-1.task-1-2 :refer :all]
            [task-1.task-test-common :refer :all]))

(deftest third-length-test
  (testing "Test sequenses 3nd length"
    (is (= three_len_set (sort (seq-all-words [] `("a" "b" "c") 3))))))

(deftest second-length-test
  (testing "Test sequenses 2nd length"
    (is (= two_len_set (sort (seq-all-words [] `("a" "b" "c") 2))))))

(deftest length-test
  (testing "Test lengths"
    (loop [i 2]
      (is (= (calc-subseq-length i 2) (count (seq-all-words [] (char-range i) 2))))
      (if (= i 26)
        ()
        (recur (inc i))))))
