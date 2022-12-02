(ns task-4.expression
  (:require [task-4.var-const :refer :all])
  (:require [clojure.string :as string]))

; заранее определяем функции, т.к. они взаимосвязаны
(defn expr? [expr])
(defn no? [expr])
(defn ||? [expr])
(defn &&? [expr])
(defn && [expr1 & exprs])
(defn || [expr1 & exprs])

(defn decart-production [colls]
  "декартово произведение всех подвыражений"
  (if (empty? colls)
    '(())
    (for [x (first colls) more (decart-production (rest colls))]
      (cons x more))))

(defn distribute-&& [exprs]
  "протаскиваем конъюнкцию внутрь подвыражений, объединяя полученные конъюнкции дизъюнкцией"
  (apply || (map #(if (> (count %) 1) (cons ::&& %) %) (decart-production (map #(if (||? %) (args&| %) (list %)) exprs)))))

(defn && [expr1 & exprs]
  "порождение конъюнкции"
  {:pre [(and (expr? expr1) (every? expr? exprs))]}
  (let [args (filter #(not (True? %)) (cons expr1 exprs))]
    (if (> (count args) 1) (distribute-&& (reduce #(concat %1 (if (&&? %2) (args&| %2) (list %2))) '() args)) (if (= (count args) 1) (first args) (constant 1)))))

(defn &&? [expr]
  "проверка, конъюнкция ли"
  (and (= (first expr) ::&&) (every? expr? (args&| expr))))

(defn || [expr1 & exprs]
  "порождение дизъюнкции"
  {:pre [(and (expr? expr1) (every? expr? exprs))]}
  (let [args (filter #(not (False? %)) (cons expr1 exprs))]
    (if (> (count args) 1) (cons ::|| (reduce #(concat %1 (if (||? %2) (args&| %2) (list %2))) '() args)) (if (= (count args) 1) (first args) (constant 0)))))

(defn ||? [expr]
  "проверка, дизъюнкция ли"
  (and (= (first expr) ::||) (every? expr? (args&| expr))))

(defn no [expr]
  "порождение отрицания"
  {:pre [(expr? expr)]}
  (if (True? expr)
    (constant 0)
    (if (False? expr)
      (constant 1)
      (if (no? expr)
        (second expr)
        (if (&&? expr)
          (apply || (map #(no %) (args&| expr)))
          (if (||? expr)
            (apply && (map #(no %) (args&| expr)))
            (list ::no expr)))))))

(defn no? [expr]
  "проверка, отрицание ли"
  (and (= (first expr) ::no) (expr? (second expr))))

(defn --> [expr1 expr2]
  "порождение импликации"
  {:pre [(and (expr? expr1) (expr? expr2))]}
  (|| (no expr1) expr2))

(defn expr? [expr]
  "валидирует, выражение ли это"
  (and (coll? expr) (or (constant? expr) (variable? expr) (&&? expr) (||? expr) (no? expr))))

(defn expr-to-str
  "Печатаем выражение во вменяемом виде"
  [expr]
  {:pre [(expr? expr)]}
  (if (or (constant? expr) (variable? expr))
    (name (first (args&| expr)))
    (if (no? expr)
      (apply str ["!" (first (map expr-to-str (args&| expr)))])
      (if (&&? expr)
        (apply str ["(" (string/join " & " (doall (map expr-to-str (args&| expr)))) ")"])
        (if (||? expr)
           (apply str ["(" (string/join " | " (doall (map expr-to-str (args&| expr)))) ")"])
           (throw (Exception. "add case")))))))

(defn z [expr variab value]
  "подстановка переменной в выражение"
  {:pre [(expr? expr)]}
  {:pre [(expr? value)]}
  {:pre [(variable? variab)]}
  (if (constant? expr)
    expr
    (if (variable? expr)
      (if (same-variables? variab expr) value expr)
      (if (no? expr)
        (no (z (second expr) variab value))
        (if (&&? expr)
          (apply && (map #(z % variab value) (args&| expr)))
          (if (||? expr)
            (apply || (map #(z % variab value) (args&| expr)))
            (throw (Exception. "add case"))))))))
