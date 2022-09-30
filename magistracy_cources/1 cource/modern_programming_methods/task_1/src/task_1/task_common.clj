(ns task-1.task-common)

(defn append-char-to-word
  "Соединяет символ со словом, если такой же символ не встречается в начале слова"
  [word symbol]
  (if (= (str (last word)) symbol)
    nil
    (str word symbol)))
