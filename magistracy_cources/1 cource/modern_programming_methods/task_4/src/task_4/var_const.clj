(ns task-4.var-const)

;организация хранения структур выражения

(defn constant [val]
  "создаём константу из val"
  {:pre [(or (= 1 val) (= 0 val))]}
  (list ::constant val))

(defn constant? [expr]
  "проверка, константа ли выражение"
  (= (first expr) ::constant))

(defn True? [expr]
  "проверка, истина ли"
  (and (constant? expr) (= (second expr) 1)))

(defn False? [expr]
  "проверка, ложь ли"
  (and (constant? expr) (= (second expr) 0)))

(defn constant-value [expr]
  "возвращает значение константы"
  (second expr))

(defn variable [name]
  "создаём переменную из val"
  {:pre [(keyword? name)]}
  (list ::variable name))

(defn variable? [expr]
  "проверка, переменная ли данное выражение"
  (= (first expr) ::variable))

(defn variable-value [expr]
  "возвращает значение переменной"
  (second expr))

(defn same-variables? [v1 v2]
  "сравнение переменных"
  (and (variable? v1) (variable? v2) (= (second v1) (second v2))))

(defn args&| [expr]
  "Получаем аргумент выражения"
  (drop 1 expr))
