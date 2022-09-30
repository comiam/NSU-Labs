(ns task-1.task-1-2
  (:require [task-1.task-common :refer :all]))

(defn concat-seq-to-word
  "Делает подпоследовательность слов, соединяя текущее слово со всеми символами из набора"
  [word seq words-acc]
  (if (empty? seq)                                          ; если соединили уже все символы со словом
    words-acc
    (let [new-word (append-char-to-word word (first seq))
          new-seq (rest seq)]
      (if (nil? new-word)                                   ; если случился повтор подряд идущих символов
        (recur word new-seq words-acc)
        (recur word new-seq (into words-acc (list new-word)))))))

(defn concat-seq-to-words
  "Делает новые подпоследовательности слов, добавляя символы"
  [words seq words-acc]
  (if (empty? words)                                        ; если перебрали все слова
    words-acc
    (recur (rest words) seq (into words-acc (concat-seq-to-word (first words) seq [])))))

(defn seq-all-words
  "Создаёт последовательность секвенций символов char-seq длины n без повторяющихся попарно символов"
  [words-acc seq n]
  (if (empty? seq)                                          ; проверка на пустой набор
    `()
    (if (empty? words-acc)                                  ; начало рекурсии
      (recur seq seq n)
      (if (= (count (first words-acc)) n)                   ; если длина слов равна n
        words-acc
        (recur (concat-seq-to-words words-acc seq []) seq n)))))

(println (seq-all-words [] `("a" "b" "c") 2))
