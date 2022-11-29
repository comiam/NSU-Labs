(ns task-3.common)

(defn split-col [step ost col]
  (let
    [part (map first
               (iterate
                 (fn [[fst [snd thd]]]
                   (if (> ost thd)
                     [(take (inc step) snd) [(drop (inc step) snd) (inc thd)]]
                     [(take step snd) [(drop step snd) (inc thd)]])
                   )
                 [col [col 0]]))]
    (fn [thread_number] (nth part thread_number))))

(defn thread-filter [cond? col thread-count]
  (let [size (count col),
        step (quot size thread-count),
        ost (mod size thread-count)]
    (->>
      (iterate inc 1)
      (take thread-count)
      (map #(future (doall (filter cond? ((split-col step ost col) %)))))
      (doall)
      (mapcat deref))))

(defn lazy-filter [cond? col part-n thread-n]
  (lazy-seq (concat (thread-filter cond? (take part-n col) thread-n)
                    (if (empty? col)
                      '()
                      (lazy-filter cond? (drop part-n col) part-n thread-n)))))
