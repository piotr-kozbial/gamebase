(ns attic.vision
  (:require  [goog.events :as events]))


;;
;; {
;;     :top-left-x
;;     :top-left-y
;;     :scale
;;     :width-in-tiles
;;     :height-in-tiles
;;     :tile-width
;;     :tile-height
;; }



nil

;; COS CO BYLO W makstycoon.util.draw:
;;
;; (defn canvas-xy-to-world-xy [geometry [canvas-x canvas-y]]
;;   (let [{:keys [top-left-x top-left-y magnif tile-height height-in-tiles]} geometry]
;;     [(+ (/ canvas-x magnif) top-left-x)
;;      (- (* tile-height height-in-tiles)
;;         (+ (/ canvas-y magnif) top-left-y))]))
;; (defn world-xy-to-tile-xy [geometry [x y]]
;;   (let [{:keys [tile-width tile-height]} geometry]
;;     [(js/floor (/ x tile-width))
;;      (js/floor (/ y tile-height))]))
;; (defn world-xy-to-scaled-canvas-xy [geometry [x y]]
;;   ;; we assume that the output will be scaled,
;;   ;; so we only need to account for translation
;;   (let [{:keys [top-left-x top-left-y height-in-tiles tile-height]} geometry]
;;     [(- x top-left-x)
;;      (- (- (* tile-height height-in-tiles) y) top-left-y)]))
;; (defn -line [geometry p1 p2]
;;   (let [[cx1 cy1] (world-xy-to-scaled-canvas-xy geometry p1)
;;         [cx2 cy2] (world-xy-to-scaled-canvas-xy geometry p2)]
;;     (js/line cx1 cy1 cx2 cy2)))
;; (defn get-tile-corners [geometry [tile-x tile-y]]
;;   (let [{:keys [tile-width tile-height]} geometry]
;;     [[(* tile-x tile-width) (* tile-y tile-height)]
;;      [(* (inc tile-x) tile-width) (* tile-y tile-height)]
;;      [(* (inc tile-x) tile-width) (* (inc tile-y) tile-height)]
;;      [(* tile-x tile-width) (* (inc tile-y) tile-height)]]))
;; (defn -tile-box [geometry [tile-x tile-y]]
;;   (let [{:keys [tile-height tile-width]} geometry
;;         [p0 p1 p2 p3] (get-tile-corners geometry [tile-x tile-y])]
;;     (-line geometry p0 p1)
;;     (-line geometry p1 p2)
;;     (-line geometry p2 p3)
;;     (-line geometry p3 p0)))
;; (defn -tile-rect [geometry [tile-x tile-y]]
;;   (let [{:keys [tile-height tile-width]} geometry
;;         [p0 _ p2 _] (get-tile-corners geometry [tile-x tile-y])
;;         [cx0 cy0] (world-xy-to-scaled-canvas-xy geometry p0)
;;         [cx2 cy2] (world-xy-to-scaled-canvas-xy geometry p2)]
;;     (js/rect cx0 cy0 (- cx2 cx0) (- cy2 cy0))))



(defonce selected-point (atom [0 0]))
(def g (atom nil))

(defn canvas-xy-to-world-xy [geometry [canvas-x canvas-y]]
  (let [{:keys [top-left-x top-left-y magnif tile-height height-in-tiles]} geometry]
    [(+ (/ canvas-x magnif) top-left-x)
     (- (* tile-height height-in-tiles)
        (+ (/ canvas-y magnif) top-left-y))]))

(defn world-xy-to-tile-xy [geometry [x y]]
  (let [{:keys [tile-width tile-height]} geometry]
    [(js/floor (/ x tile-width))
     (js/floor (/ y tile-height))]))

(defn world-xy-to-scaled-canvas-xy [geometry [x y]]
  ;; we assume that the output will be scaled,
  ;; so we only need to account for translation
  (let [{:keys [top-left-x top-left-y height-in-tiles tile-height]} geometry]
    [(- x top-left-x)
     (- (- (* tile-height height-in-tiles) y) top-left-y)]))

(defn -line [geometry p1 p2]
  (let [[cx1 cy1] (world-xy-to-scaled-canvas-xy geometry p1)
        [cx2 cy2] (world-xy-to-scaled-canvas-xy geometry p2)]
    (js/line cx1 cy1 cx2 cy2)))

(defn get-tile-corners [geometry [tile-x tile-y]]
  (let [{:keys [tile-width tile-height]} geometry]
    [[(* tile-x tile-width) (* tile-y tile-height)]
     [(* (inc tile-x) tile-width) (* tile-y tile-height)]
     [(* (inc tile-x) tile-width) (* (inc tile-y) tile-height)]
     [(* tile-x tile-width) (* (inc tile-y) tile-height)]]))

(defn -tile-box [geometry [tile-x tile-y]]
  (let [{:keys [tile-height tile-width]} geometry
        [p0 p1 p2 p3] (get-tile-corners geometry [tile-x tile-y])]
    (-line geometry p0 p1)
    (-line geometry p1 p2)
    (-line geometry p2 p3)
    (-line geometry p3 p0)))

(defn -marker [geometry [x y]]
  (-line geometry [(- x 5) y] [(+ x 5) y])
  (-line geometry [x (- y 5)] [x (+ y 5)]))

(defn tile-at [geometry {:keys [content height width]} p]

  (let [[tile-x tile-y] (world-xy-to-tile-xy geometry p)]
    (when (and (<= 0 tile-x) (< tile-x width)
               (<= 0 tile-y) (< tile-y height))
      (-> content
          (nth (- (dec height) tile-y))
          (nth tile-x)))))

(defn cross-points [geometry terrain p1 p2]
  (let [{:keys [tile-width tile-height]} geometry
        [x1 y1] p1, [x2 y2] p2]
    (doall
     (concat
      (when (> (js/abs (- x1 x2)) tile-width)
        (if (< x1 x2)
          ;; we start at x1 rounded up to tile boundary and increment in tile-width steps
          ;; until we pass x2
          (let [x0 (* tile-width (js/ceil (/ x1 tile-width)))
                xEnd (* tile-width (js/floor (/ x2 tile-width)))
                a (/ (- y2 y1) (- x2 x1))]
            (for [x (take-while #(< % xEnd) (iterate (partial + tile-width) x0))]
              [(inc x) (+ y1 (* a (- x x1)))]))
          ;; we start at x1 rounded down to tile boundary and decrement in tile-width steps
          ;; until we pass x2
          (let [x0 (* tile-width (js/floor (/ x1 tile-width)))
                xEnd (* tile-width (js/ceil (/ x2 tile-width)))
                a (/ (- y2 y1) (- x2 x1))]
            (for [x (take-while #(> % xEnd) (iterate #(- % tile-width) x0))]
              [(dec x) (+ y1 (* a (- x x1)))]))))
      (when (> (js/abs (- y1 y2)) tile-height)
        (if (< y1 y2)
          (let [y0 (* tile-height (js/ceil (/ y1 tile-height)))
                yEnd (* tile-height (js/floor (/ y2 tile-height)))
                a (/ (- x2 x1) (- y2 y1))]
            (for [y (take-while #(< % yEnd) (iterate (partial + tile-height) y0))]
              [(+ x1 (* a (- y y1))) (inc y)]))
          (let [y0 (* tile-height (js/floor (/ y1 tile-height)))
                yEnd (* tile-height (js/ceil (/ y2 tile-height)))
                a (/ (- x2 x1) (- y2 y1))]
            (for [y (take-while #(> % yEnd) (iterate #(- % tile-height) y0))]
              [(+ x1 (* a (- y y1))) (dec y)]))))))))

(defn line-crosses-terrain? [geometry terrain p1 p2]
  (->> (cross-points geometry terrain p1 p2)
       ;; ((fn [pts]
       ;;    (doseq [p pts]
       ;;      (js/stroke
       ;;       (apply js/color
       ;;        (if (= 0 (tile-at geometry terrain p))
       ;;          [0 0 200]
       ;;          [200 0 0])))
       ;;      (-marker geometry p))
       ;;    pts))

       (map (partial tile-at geometry terrain))
       (some #(not= 0 %))))

(defn distanceSquared [[x1 y1] [x2 y2]]
  (let [dx (- x1 x2)
        dy (- y1 y2)]
    (+ (* dx dx) (* dy dy))))

(defn tile-visible-from? [geometry terrain p tile-xy]
  (some
   #(and
     (< (distanceSquared p %) (* 200 200))

     (not (line-crosses-terrain? geometry terrain p %))

     )

   (get-tile-corners geometry tile-xy)))

(defn draw [{:keys [top-left-x top-left-y magnif] :as geometry} terrain]
  (reset! g geometry)

  (when (and (> (:height-in-tiles geometry) 1)
             (> (:width-in-tiles geometry) 1)
             (> (:tile-width geometry) 1)
             (> (:tile-height geometry) 1)
             )


    (let [[x y] (canvas-xy-to-world-xy geometry [js/mouseX js/mouseY])
          [tile-x tile-y] (world-xy-to-tile-xy geometry [x y])
          point @selected-point]

      (js/resetMatrix)
      (js/scale magnif)


      ;; tile under cursor
      (js/stroke (js/color 100 100 100))
      (-tile-box geometry [tile-x tile-y])

      ;; selected point
      (js/stroke (js/color 0 0 200))
      (-marker geometry point)

      ;; lines from point to tile
      (js/stroke (js/color 200 100 100))
      (doseq [p (get-tile-corners geometry [tile-x tile-y])]
        (js/stroke
         (apply js/color
           (if (line-crosses-terrain? geometry terrain point p)
             [200 0 0]
             [0 200 0])))
        (-line geometry point p))

      (js/stroke
       (apply js/color
         (if (tile-visible-from? geometry terrain point [tile-x tile-y])
             [0 200 0]
             [200 0 0])))
      (-tile-box geometry [tile-x tile-y]))))
