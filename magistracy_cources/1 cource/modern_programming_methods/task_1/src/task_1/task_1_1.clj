(ns task-1.task_1_1)

(defn append_char_to_word
  "Соединяет символ со словом, если такой же символ не встречается в начале слова"
  [word char]
  (if (= (first word) char)
    word
    (str char word)))

(defn concat_seq_to_word
  "Делает подпоследовательность слов, соединяя текущее слово со всеми символами из набора"
  [word seq seq_i words_acc]
  (if (= seq_i (count seq))                                 ; если соединили уже все символы со словом
    words_acc
    (let [new_word (append_char_to_word word (nth seq seq_i))
          new_seq (inc seq_i)]
      (if (= word new_word)                                 ; если случился повтор подряд идущих символов
        (concat_seq_to_word word seq new_seq words_acc)
        (concat_seq_to_word word seq new_seq (doall (concat words_acc (list new_word))))))))

(defn concat_seq_to_words
  "Делает новые подпоследовательности слов, добавляя символы"
  [words seq words_acc words_i]
  (if (= (count words) words_i)                             ; если перебрали все слова
    words_acc
    (concat_seq_to_words words seq (doall (concat words_acc (concat_seq_to_word (nth words words_i) seq 0 []))) (inc words_i))))

(defn seq_all_words
  "Создаёт последовательность секвенций символов char_seq длины n без повторяющихся попарно символов"
  [words_acc seq n]
  (if (= 0 (count seq))                                     ; проверка на пустой набор
    []
    (if (= 0 (count words_acc))                             ; начало рекурсии
      (seq_all_words (map str seq) seq n)
      (if (= (count (nth words_acc 0)) n)                   ; если длина слов равна n
        words_acc
        (seq_all_words (concat_seq_to_words words_acc seq [] 0) seq n)))))

;(println (seq_all_words [] `(\a \b \c) 2))
