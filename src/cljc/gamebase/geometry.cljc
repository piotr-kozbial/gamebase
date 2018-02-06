(ns gamebase.geometry)

(defn deg
  "Create angle (in \"radians\") from given number of degrees"
  [degrees]
  (double (* degrees (/ Math/PI 180))))

(def deg0 (deg 0))
(def deg90 (deg 90))
(def deg180 (deg 180))
(def deg270 (deg 270))
(def deg360 (deg 360))

(defn to-deg [angle]
  (double (* angle (/ 180 Math/PI))))

(defn normalize-angle [angle]
  (if (>= angle 0)
    (let [n (int (/ angle (* 2 Math/PI)))]
      (- angle (* n 2 Math/PI)))
    (let [k (int (/ (- angle) (* 2 Math/PI)))]
      (+ angle (* (inc k) 2 Math/PI)))))

(defn arc [xcenter ycenter, radius, angle-start angle-end]
  {:curve :arc
   :xcenter xcenter
   :ycenter ycenter
   :radius radius
   :angle-start angle-start
   :angle-end angle-end})

(defn segment [x1 y1 x2 y2]
  {:curve :segment
   :x1 x1
   :y1 y1
   :x2 x2
   :y2 y2})

(defn rev [curve]
  (case (:curve curve)
    :arc (assoc curve
                :x1 (:x2 curve)
                :y1 (:y2 curve)
                :x2 (:x1 curve)
                :y2 (:y1 curve))
    :segment (assoc curve
                    :angle-start (:angle-end curve)
                    :angle-end (:angle-start curve))))

(defn arc-length
  ([radius angle]
   (Math/abs (* 2 Math/PI radius (/ angle deg360))))
  ([arc]
   (arc-length (:radius arc)
               (- (:angle-end arc) (:angle-start arc)))))


;; Tile geometry
;;
;; L - length of a side of a tile
;;
;; 0,L---:n----L,L
;;  |           |
;;  |           |
;; :w          :e
;;  |           |
;;  |           |
;; 0,0---:s----L,0

(def tile-side-length 32)

(def tile-circle-length
  (Math/abs (* 2 Math/PI (/ tile-side-length 2))))

(def tile-arc-length
  (Math/abs (* 0.5 Math/PI (/ tile-side-length 2))))

(defn tile-curve-length [from to]
  (case [from to]
    [:n :s] tile-side-length
    [:s :n] tile-side-length
    [:w :e] tile-side-length
    [:e :w] tile-side-length
    tile-arc-length))

(defn tile-arc [from to]
  (let [L tile-side-length]
    (case [from to]
      [:n :w] (arc 0 L, (/ L 2), deg0 (- deg90))
      [:w :n] (arc 0 L, (/ L 2), (- deg90) deg0)

      [:n :e] (arc L L, (/ L 2), deg180 deg270)
      [:e :n]  (arc L L, (/ L 2), deg270 deg180)

      [:s :w]  (arc 0 0, (/ L 2), deg0 deg90)
      [:w :s]  (arc 0 0, (/ L 2), deg90 deg0)

      [:s :e]  (arc L 0, (/ L 2), deg180 deg90)
      [:e :s]  (arc L 0, (/ L 2), deg90 deg180))))

(defn tile-segment [from to]
  (let [L tile-side-length]
    (case [from to]
      [:n :s] (segment (/ L 2) L (/ L 2) 0)
      [:s :n] (segment (/ L 2) 0 (/ L 2) L)

      [:w :e] (segment 0 (/ L 2) L (/ L 2))
      [:e :w] (segment L (/ L 2) 0 (/ L 2)))))

(defn tile-curve [from to]
  (case [from to]
    [:n :s] (tile-segment :n :s)
    [:s :n] (tile-segment :s :n)
    [:w :e] (tile-segment :w :e)
    [:e :w] (tile-segment :e :w)
    (tile-arc from to)))

(def ne-arc (tile-arc :n :e)) (def en-arc (tile-arc :e :n))
(def es-arc (tile-arc :e :s)) (def se-arc (tile-arc :s :e))
(def sw-arc (tile-arc :s :w)) (def ws-arc (tile-arc :w :s))
(def wn-arc (tile-arc :w :n)) (def nw-arc (tile-arc :n :w))

(def ns-segment (tile-segment :n :s))
(def sn-segment (tile-segment :s :n))
(def we-segment (tile-segment :w :e))
(def ew-segment (tile-segment :e :w))

(defn curve-section [curve start-ratio end-ratio]
  (case (:curve curve)
    :segment
    (let [{:keys [x1 y1 x2 y2]} curve
          dx (- x2 x1)
          dy (- y2 y1)]
      (assoc curve
             :x1 (+ x1 (* dx start-ratio))
             :x2 (+ x1 (* dx end-ratio))
             :y1 (+ y1 (* dy start-ratio))
             :y2 (+ y1 (* dy end-ratio))))
    :arc
    (let [{:keys [xcenter ycenter radius
                  angle-start angle-end]} curve
          dangle (- angle-end angle-start)]
      (assoc curve
             :angle-start (+ angle-start (* dangle start-ratio))
             :angle-end (+ angle-start (* dangle end-ratio))))))

(defn point-on-curve [curve by value] ;; len
  (case by

    :len

    (case (:curve curve)
      :segment
      (let [{:keys [x1 y1 x2 y2]} curve
            len value]
        (cond
          (= (double x1) (double x2)) (if (< y1 y2)
                                        [x1 (+ y1 len) deg90]
                                        [x1 (- y1 len) deg270])
          (= (double y1) (double y2)) (if (< x1 x2)
                                        [(+ x1 len) y1 deg0]
                                        [(- x1 len) y1 deg180])
          :else nil ;;(throw (Exception. (str "NOT IMPLEMENTED for " curve)))

          ))
      :arc
      (let [{:keys [xcenter ycenter radius angle-start angle-end]} curve
            len value
            angle (if (< angle-start angle-end)
                    (+ angle-start (* 2 Math/PI (/ len tile-circle-length)))
                    (- angle-start (* 2 Math/PI (/ len tile-circle-length))))]
        [(+ xcenter (* radius (Math/cos angle)))
         (+ ycenter (* radius (Math/sin angle)))
         (normalize-angle
          (if (< angle-start angle-end)
            (+ angle (/ Math/PI 2))
            (- angle (/ Math/PI 2))))]))

    :rlen
    (let [[x y angle]
          (case (:curve curve)
            :segment
            (let [{:keys [x1 y1 x2 y2]} curve]
              (point-on-curve {:curve :segment, :x1 x2 :y1 y2 :x2 x1 :y2 y1} :len value))
            :arc
            (let [{:keys [xcenter ycenter radius angle-start angle-end]} curve
                  len value
                  angle (if (< angle-start angle-end)
                          (+ angle-start (* 2 Math/PI (/ len tile-circle-length)))
                          (- angle-start (* 2 Math/PI (/ len tile-circle-length))))]
              (point-on-curve
               {:curve :arc, :xcenter xcenter, :ycenter ycenter, :radius radius, :angle-start angle-end, :angle-end angle-start}
               :len
               value)))]
      [x y (normalize-angle (+ Math/PI angle))])

    :ratio

    (case (:curve curve)
      :segment
      (let [{:keys [x1 y1 x2 y2]} curve
            ratio value]
        (cond
          (= (double x1) (double x2)) (let [len (Math/abs (- y2 y1))]
                                          (if (< y1 y2)
                                            [x1 (+ y1 (* ratio len)) deg90]
                                            [x1 (- y1 (* ratio len)) deg270]))
          (= (double y1) (double y2)) (let [len (Math/abs (- x2 x1))]
                                        (if (< x1 x2)
                                          [(+ x1 (* ratio len)) y1 deg0]
                                          [(- x1 (* ratio len)) y1 deg180]))
          :else nil ;;(throw (Exception. (str "NOT IMPLEMENTED for " curve)))
          ))
      :arc
      (let [{:keys [xcenter ycenter radius angle-start angle-end]} curve
            ratio value
            angle (if (< angle-start angle-end)
                    (+ angle-start (* 0.5 Math/PI ratio))
                    (- angle-start (* 0.5 Math/PI ratio)))]
        [(+ xcenter (* radius (Math/cos angle)))
         (+ ycenter (* radius (Math/sin angle)))
         (normalize-angle
          (if (< angle-start angle-end)
            (+ angle (/ Math/PI 2))
            (- angle (/ Math/PI 2))))]))))

;; Multi-Path Length calculations (for tile curves)
;;
;; a multi-path is: a sequence of elements, each of each is one of:
;;   - a tile curve [from to]
;;   - a triple [curve :len length]
;;   - a triple [curve :rlen length]
;;   - a triple [curve :ratio ratio]
;;   - a triple [curve :rratio ratio]
;;
;; (:rlen and :rratio mean "from this len/ratio to curve end")
;;
;; we need to be able to:
;;   - calculate total length of a multi-path
;;   - reverse a multi-path
;;   - find the point at a given distance from the start of a multi-path
;;     (return the sequential number of the multi-path element
;;     and length/ratio inside it)

(defn multi-path-element-length [element]
  (let [calculate-for-curve
        (fn [[from to]] (tile-curve-length from to))
        calculate-for-len
        (fn [[curve _ len]] (Math/abs len))
        calculate-for-ratio
        (fn [[[from to] _ ratio]] (* (Math/abs ratio) (tile-curve-length from to)))]
    (case (count element)
      2 (calculate-for-curve element)
      3 (case (second element)
          :len (calculate-for-len element)
          :rlen (calculate-for-len element)
          :ratio (calculate-for-ratio element)
          :rratio (calculate-for-ratio element)))))

(defn multi-path-length [path]
  (->> path
       (map multi-path-element-length)
       (reduce + 0)))

;; For example:
(assert
 (= (multi-path-length
     [[:e :w]
      [:e :w]
      [:e :w]
      [[:e :w] :ratio 0.25]])
    104.0))

(defn reverse-multi-path-element [element]
  (case (count element)
    2 (let [[x y] element] [y x])
    3 (let [[[from to] op val] element]
        (case op
          :len [[to from] :rlen val]
          :rlen [[to from] :len val]
          :ratio [[to from] :rratio val]
          :rratio [[to from] :ratio val]))))

(defn reverse-multi-path [path]
  (->> path
       (reverse)
       (map reverse-multi-path-element)))

;; For example:
(assert
 (= (apply vector
      (reverse-multi-path
       [[:e :w]
        [:e :w]
        [:e :n]
        [[:s :n] :ratio 0.2]]))
    [[[:n :s] :rratio 0.2]
     [:n :e]
     [:w :e]
     [:w :e]]))

;; return [path-number-0-based, len-inside-last-elem]
;; or nil if total-len too big
(defn find-point-on-multi-path-by-length [path total-len]
  (loop [path-left path
         len-left (double total-len)
         n 0]
    (if (empty? path-left)
      nil
      (let [len (multi-path-element-length (first path-left))]
        (if (<= len-left len)
          [n len-left]
          (recur (rest path-left) (- len-left len) (inc n)))))))

;; For example:
(assert
 (= (find-point-on-multi-path-by-length
     [[:e :w]
      [:e :w]
      [:e :w]
      [[:e :w] :ratio 0.25]]
     102)
    [3 6.0]))
(assert
 (= (find-point-on-multi-path-by-length
     [[[:e :w] :rratio 0.25]
      [:e :w]
      [:e :w]
      [:e :w]]
     102)
    [3 30.0]))
(assert
 (=
  (find-point-on-multi-path-by-length [[:e :w] [:e :w]] 148)
  nil))
