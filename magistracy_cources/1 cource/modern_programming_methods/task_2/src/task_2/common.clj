(ns task-2.common)

(defn linear [x] x)
(defn parabola [x] (* x x))
; f(x)=x^4 - 20x^2 + 3x + 1
(defn polynomial [x] (+ (- (* (* (* x x) x) x) (* 20 (* x x))) (* 3 x) 1))

(defn trapezoid [f a b]
  ; h = b - a
  ; (f(x_i-1) + f(x_i))/2 - (f(x_i) - f(x_i-1))
  (* (* (+ (f a) (f b)) (- b a)) 0.5) )

(defn integral [f a b h]
  (if (< a b)
    (+
      (trapezoid f a (+ a h))
      (integral f (+ a h) b h)
      )
    0
    ) )

(def integral-mem (memoize integral))
