(ns task-1.task-1-3)

(defn my-map
  [function collection]
  (seq (reduce #(conj %1 (function %2)) [] collection)))

(defn my-filter
  [predicate collection]
  (seq (reduce #(if (predicate %2) (conj %1 %2) %1) [] collection)))

(println (my-map inc `(1 3 5)))
(println (my-filter even? `(1 2 3 4 5)))