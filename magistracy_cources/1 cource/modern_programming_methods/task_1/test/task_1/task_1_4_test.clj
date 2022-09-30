(ns task-1.task-1-4-test
  (:require [clojure.test :refer :all]
            [task-1.task-1-4 :refer :all]
            [task-1.task-test-common :refer :all]))

(deftest third-length-test
  (testing "Test sequences 3nd length"
    (is (= three_len_set (sort (seq-all-words `("a" "b" "c") 3))))))

(deftest second-length-test
  (testing "Test sequences 2nd length"
    (is (= two_len_set (sort (seq-all-words `("a" "b" "c") 2))))))

(deftest length-test
  (testing "Test lengths"
    (loop [word_len 2]
      (loop [i 2]
        (is (= (calc-subseq-length i word_len) (count (seq-all-words (char-range i) word_len))))
        (if (= i 10)
          ()
          (recur (inc i))))
      (if (= word_len 5)
        ()
        (recur (inc word_len))))))
