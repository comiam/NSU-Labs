(ns task-4.task-4-test
  (:require [clojure.test :refer :all]
            [task-4.expression :refer :all]
            [task-4.var-const :refer :all]))

(deftest z4-tests
  (testing "constant"
    (is (constant? (constant 1)))
    (is (constant? (constant 0)))
    (is (True? (constant 1)))
    (is (False? (constant 0)))
    (is (= 1 (constant-value (constant 1))))
    (is (= 0 (constant-value (constant 0)))))
  (testing "variable"
    (is (variable? (variable :a)))
    (is (= :a (variable-value (variable :a))))
    (is (same-variables? (variable :a) (variable :a)))
    (is (= false (same-variables? (variable :a) (variable :b)))))
  (testing "expr?"
    (is (expr? (constant 1)))
    (is (expr? (variable :a)))
    (is (= false (expr? (list 1))))
    (is (= false (expr? 0)))
    (is (expr? (&& (constant 1) (constant 0))))
    (is (expr? (|| (constant 1) (constant 0))))
    (is (expr? (--> (constant 1) (constant 0))))
    (is (expr? (no (constant 1))))
    (is (= false (expr? (list ::&& 0 0)))))
  (testing "&&"
    (is (&&? (&& (variable :a) (variable :b))))
    (is (= (&& (variable :a) (&& (variable :c) (variable :b))) (&& (variable :a) (variable :c) (variable :b))))
    (is (= (&& (constant 1) (constant 0)) (constant 0))))
    (is (= (&& (variable :a) (|| (variable :c) (variable :b))) (|| (&& (variable :a) (variable :c)) (&& (variable :a) (variable :b))))))
  (testing "||"
    (is (||? (|| (variable :a) (variable :b))))
    (is (= (|| (variable :a) (|| (variable :c) (variable :b))) (|| (variable :a) (variable :c) (variable :b))))
    (is (= (|| (constant 1) (constant 0)) (constant 1))))
  (testing "импликация"
    (is (= (--> (no (variable :a)) (variable :b)) (|| (variable :a) (variable :b))))
    (is (= (--> (variable :a) (variable :b)) (|| (no (variable :a)) (variable :b)))))
  (testing "отрицание"
    (is (no? (no (variable :b))))
    (is (= (no (&& (variable :a) (variable :b))) (|| (no (variable :a)) (no (variable :b)))))
    (is (= (no (constant 1)) (constant 0)))
    (is (= (no (constant 0)) (constant 1)))
    (is (= (no (|| (variable :a) (variable :b))) (&& (no (variable :a)) (no (variable :b))))))
  (testing "z"
    (is (= (z (&& (variable :a) (variable :b)) (variable :b) (constant 1)) (variable :a)))
    (is (= (z (no (&& (variable :a) (variable :b))) (variable :b) (constant 1)) (no (variable :a))))
    (is (= (z (&& (variable :a) (variable :b)) (variable :b) (|| (variable :c) (variable :d))) (|| (&& (variable :a) (variable :c)) (&& (variable :a) (variable :d))))))
