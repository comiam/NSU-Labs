(ns task-2.common)

(defn linear [x] x)
(defn parabola [x] (* x x))
; f(x)=x^4 - 20x^2 + 3x + 1
(defn polynomial [x] (+ (- (* (* (* x x) x) x) (* 20 (* x x))) (* 3 x) 1))

(defn trapezoid [f a b]
  ; h = b - a
  (* (* (+ (f a) (f b)) (- b a)) 0.5))

(defn integral [f a b h]
  (if (< a b)
    (+
      (trapezoid f a (+ a h))
      (integral f (+ a h) b h)
      )
    0
    ))

(def integral-mem (memoize integral))

(defn inf-integral [f step]
  (let [seq (map first (iterate (fn [[step_sum index]]
                                  [(+ step_sum (trapezoid f (* step (- index 1)) (* step index))) (inc index)]) [0 1]))]
    (fn [x]
      (let [i (int (/ x step))] (+
                                  (nth seq i)
                                  (trapezoid f (* step i) x)) )
      )
    )
  )