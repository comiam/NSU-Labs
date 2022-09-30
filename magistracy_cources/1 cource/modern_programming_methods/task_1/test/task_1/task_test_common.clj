(ns task-1.task-test-common
  (:require [clojure.test :refer :all]))

(def two_len_set `("ab" "ac" "ba" "bc" "ca" "cb"))
(def three_len_set `("aba" "abc" "aca" "acb" "bab" "bac" "bca" "bcb" "cab" "cac" "cba" "cbc"))

(defn char-range [len]
  (map str (map char (range 97 (+ 97 len)))))

(defn calc-subseq-length
  ([char-len len]
   (calc-subseq-length (dec len) (dec char-len) char-len))
  ([cur-ind factor acc]
   (if (= 0 cur-ind)
     acc
     (calc-subseq-length (dec cur-ind) factor (* factor acc)))))