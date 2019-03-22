;;;;; STATUS: TO BE INVESTIGATED ;;;;;

(ns gamebase.projection)

;;
;;
;;                 y
;;                   ^            wc
;;                   |         |------|
;;                   |
;;                   |       y"
;;                   |         ^
;;                 y'|   _     |....... V_
;;                   ^   |     |      :
;;                   | hc|     |      :
;;                   |   -  ---+-------->
;;                   |         |V0       x"
;;                   |         |
;;                   |
;;  -----------------+------------>----------->
;;                   |W0           x'          x
;;                   |
;;                   |
;;                   |
;;                   |
;;                   |
;;                   |
;;                   |
;;                   |
;;                   |
;;                   |
;;
nil

;; Notes.
;;
;; When used in clj, we should maybe (optionally?) turn everything to doubles
;; in constructors. In cljs it doesn't matter, because everything is double.
nil

;; Helpers
(defn absolute [a]
  (if (>= a 0)
    a
    (- a)))

;; Projection config
(defn projection-config [k tx ty wc hc]
  {:k k   ;; scaling factor
   :tx tx ;; translation in X
   :ty ty ;; translation in Y
   :wc wc ;; canvas (viewport) width *in canvas coordinates*
   :hc hc ;; canvas (viewport) height *in canvas coordinates*
   })

;; Coordinate systems
(declare world-point scaled-point view-point)

(defn world-point
  ([[x y]] (assert (number? x)) (assert (number? y))
   [:world x y])
  ([{:keys [k tx ty] :as conf} [coord-sys x y :as p]]
   (case coord-sys
     :world p
     :scaled [:world (/ x k) (/ y k)]
     :view (world-point conf (scaled-point conf p)))))

(defn scaled-point
  ([[x y]] (assert (number? x)) (assert (number? y))
   [:scaled x y])
  ([{:keys [k tx ty]} [coord-sys x y :as p]]
   (case coord-sys
     :world [:scaled (* k x) (* k y)]
     :scaled p
     :view [:scaled (- x tx) (- y ty)])))

(defn view-point
  ([[x y]] (assert (number? x)) (assert (number? y))
   [:view x y])
  ([{:keys [k tx ty] :as conf} [coord-sys x y :as p]]
   (case coord-sys
     :world (view-point conf (scaled-point conf p))
     :scaled [:view (+ x tx) (+ y ty)]
     :view p)))

(defn world-x [conf p]
  (let [[_ x _] (world-point conf p)] x))

(defn world-y [conf p]
  (let [[_ _ y] (world-point conf p)] y))

(defn world-coords [conf p]
  (let [[_ x y] (world-point conf p)] [x y]))

(defn scaled-x [conf p]
  (let [[_ x _] (scaled-point conf p)] x))

(defn scaled-y [conf p]
  (let [[_ _ y] (scaled-point conf p)] y))

(defn scaled-coords [conf p]
  (let [[_ x y] (scaled-point conf p)] [x y]))

(defn view-x [conf p]
  (let [[_ x _] (view-point conf p)] x))

(defn view-y [conf p]
  (let [[_ _ y] (view-point conf p)] y))

(defn view-coords [conf p]
  (let [[_ x y] (view-point conf p)] [x y]))

;; Lengths
(declare world-length scaled-length view-length)

(defn world-length
  ([l] (assert (number? l)) [:world l])
  ([{:keys [k] :as conf} [coord-sys l' :as l]]
   (case coord-sys
     :world l
     :scaled [:world (/ l' k)]
     :view (world-length conf (scaled-length conf l)))))

(defn scaled-length
  ([l] (assert (number? l)) [:scaled l])
  ([{:keys [k]} [coord-sys l' :as l]]
   (case coord-sys
     :world [:scaled (* k l')]
     :scaled l
     :view [:scaled l']
     )))

(defn view-length
  ([l] (assert (number? l)) [:view l])
  ([{:keys [k] :as conf} [coord-sys l' :as l]]
   (case coord-sys
     :world (view-length conf (scaled-length conf l))
     :scaled [:view l']
     :view l)))

;; Spans (distances between points in horizontal or vertical)

(defn world-span-x [p1 p2]
  (let [[_ x1 _] (world-point p1)
        [_ x2 _] (world-point p2)]
    (world-length (absolute (- x2 x1)))))

(defn world-span-y [p1 p2]
  (let [[_ _ y1] (world-point p1)
        [_ _ y2] (world-point p2)]
    (world-length (absolute (- y2 y1)))))

(defn scaled-span-x [p1 p2]
  (let [[_ x1 _] (scaled-point p1)
        [_ x2 _] (scaled-point p2)]
    (scaled-length (absolute (- x2 x1)))))

(defn scaled-span-y [p1 p2]
  (let [[_ _ y1] (scaled-point p1)
        [_ _ y2] (scaled-point p2)]
    (scaled-length (absolute (- y2 y1)))))

(defn view-span-x [p1 p2]
  (let [[_ x1 _] (view-point p1)
        [_ x2 _] (view-point p2)]
    (view-length (absolute (- x2 x1)))))

(defn view-span-y [p1 p2]
  (let [[_ _ y1] (view-point p1)
        [_ _ y2] (view-point p2)]
    (view-length (absolute (- y2 y1)))))

;; Specific points

(def W0 (world-point [0 0]))

(def V0 (view-point [0 0]))

(defn V_ [{:keys [wc hc]}] (view-point [wc hc]))

(defn Vc [conf]
  (let [[x0 y0] (view-coords conf V0)
        [x1 y1] (view-coords conf (V_ conf))]
    (view-point [(/ (+ x0 x1) 2) (/ (+ y0 y1) 2)])))


(comment ;; testing

 (def conf (projection-config 2 10 5 14 8))

 (world-point conf (world-point [2 3]))
 (world-x conf (world-point [2 3]))
 (world-y conf (world-point [2 3]))
 (view-x conf (world-point [2 3]))
 (view-y conf (world-point [2 3]))

 V0
 (V_ conf)
 (Vc conf)

 (world-point conf (Vc conf))


 )
