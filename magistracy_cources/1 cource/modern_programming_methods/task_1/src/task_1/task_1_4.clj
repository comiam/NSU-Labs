(ns task-1.task-1-4
  (:require [task-1.task-1-3 :refer :all])
  (:require [task-1.task-common :refer :all]))

(defn concat-seq-to-word
  "Делает подпоследовательность слов, соединяя текущее слово со всеми символами из набора"
  [word seq]
  (my-filter some? (my-map #(append-char-to-word word %1) seq)))

(defn concat-seq-to-words
  "Делает новые подпоследовательности слов, добавляя символы"
  [words seq]
  (reduce #(into %1 (concat-seq-to-word %2 seq)) `() words))

(defn seq-all-words
  "Создаёт последовательность секвенций символов char-seq длины n без повторяющихся попарно символов"
  [seq n]
  (if (empty? seq)
    `()
    (reduce (fn [acc _] (if (empty? acc) seq (concat-seq-to-words acc seq))) [] (range 0 n))))

;(println (seq-all-words `("a" "b" "c") 2))
