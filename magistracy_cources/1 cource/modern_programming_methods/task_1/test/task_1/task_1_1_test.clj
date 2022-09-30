(ns task-1.task_1_1_test
  (:require [clojure.test :refer :all]
            [task-1.task_1_1 :refer :all]))

(deftest third_length_test
  (testing "Test sequenses 3nd length"
    (is (= `("aba" "abc" "aca" "acb" "bab" "bac" "bca" "bcb" "cab" "cac" "cba" "cbc")
           (sort (seq_all_words [] `(\a \b \c) 3))))))

(deftest second_length_test
  (testing "Test sequenses 2nd length"
    (is (= `("ab" "ac" "ba" "bc" "ca" "cb")
           (sort (seq_all_words [] `(\a \b \c) 2))))))

(defn char_range [len]
  (map char (range 97 (+ 97 len))))

(defn calc_subseq_length
  ([char_len len]
   (calc_subseq_length (dec len) (dec char_len) char_len))
  ([cur_ind factor acc]
   (if (= 0 cur_ind)
     acc
     (calc_subseq_length (dec cur_ind) factor (* factor acc)))))

(deftest length_test
  (testing "Test lengths"
    (loop [i 2]
      (is (= (calc_subseq_length i 2) (count (seq_all_words [] (char_range i) 2))))
      (if (= i 26)
        ()
        (recur (inc i))))))
