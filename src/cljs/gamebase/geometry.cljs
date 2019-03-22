;;;;; STATUS: ON HOLD ;;;;;

(ns gamebase.geometry
  (:require-macros [gamebase.helpers :refer [examples]]))


;;;# Overview
(do
;;; This module defines some cartesian geometry
;;; to the extent we think is useful in games.
  )

;;;# Utilities
(do

  (defn sqrt [x] (Math/sqrt x))
  (defn sin [x] (Math/sin x))
  (defn cos [x] (Math/cos x))
  ;; to prevent a warning in cljs
  (declare =>)

  (def pi Math/PI)

  (def radian-to-degree-coeff (/ pi 180))
  (def degree-to-radian-coeff (/ 180 pi))
  )

;;;# Points
(do
;;; Not to complicate matters, we'll represent points
;;; as sequences of x and y, e.g.:
  [2 5]
  '(2 5)
;;; - both represent a point with x=2 and y=5.
  )

;;;# Angles
(do

;;;## Representations
  (do
;;; We will represent angles in different ways:

;;; - analytic (radians):
    {:radians 3.14}

;;; - degrees:
    {:degrees 30}

;;; - as slope (vector dx dy):
    {:slope [2 1]}

;;; *Note. We might also want a representation as tangent/cotangent value,*
;;; *but that is not as much useful, because of difficulties in encompassing*
;;; *the whole circle (360 degrees).*

;;; The slope representation encompasses the whole 360 degrees, as it is
;;; understood as a vector. So |{:slope [1 1]}| means 45 degrees,
;;; while |{:slope [-1 -1]}| represents 225 degrees (45 + 180), or -135 degrees,
;;; which is the same. In conversions, we will produce radiW każdym raziean or degree values
;;; between -180 degrees (inclusive) and 180 degrees (exclusive).

;;; In contexts where an angle is expected but a number is given,
;;; we will assume that *the number represents the angle in radians*.

;;; We will allow more than representation at the same time, e.g.
    {:degrees 45, :slope [1 1]}

;;; In such cases we will assume, *at the responsibility of the creator*
;;; *of such a value*, that all representations describe the same angle
;;; (with some approximation allowed of course).
    )

;;;## Trigonometry preparations
  (do
;;; We'll need some trigonometry to define conversions between radians or degrees and slopes.

;;;### Reverse tangent function
    (do
;;; We'll define inverted tangent (ATAN) for our purposes in the codomain
;;; of -45 to 45 degrees (the following is the cartesian geometry plane):

      ;;                                        45 deg
      ;;                              y       / dy/dx=1
      ;;                              ^     /#
      ;;                          \   |   /###
      ;;                            \ | /#####
      ;;                           ---X----->x
      ;;                            / | \#####
      ;;                          /       \###
      ;;                                    \#
      ;;                                      \ -45 deg
      ;;                                        dy/dx=-1

;;; and so ATAN goes from -45 degrees for dy/dx=-1, through 0 degrees for dy/dx=0
;;; to 45 degrees for dy/dx=1. So the plot of our ATAN is as follows (only the
;;; asterisks are in our domain):

      ;;                              angle [degrees]
      ;;                                 ^
      ;;                                 |     .-'''' ATAN
      ;;                             135-+ - *'
      ;;                            -1   | * |
      ;;                       ------+---*---+---------> dy/dx
      ;;                             | * |   1
      ;;                            .* - +-45
      ;;                      ....-'     |
      ;;                                 |
      ;;

;;; In this domain and codomain we define our atan function. The standard Java function suffices:
      (defn atan [dy-to-dx]
        (Math/atan dy-to-dx)))

;;;### Reverse cotangent function
    (do
;;; Similarly we'll define inverted cotangent (ACOTAN) for our purposes in the codomain
;;; of 45 to 135 degrees (the following is the cartesian geometry plane):

      ;;                      135 deg          45 deg
      ;;                      dx/dy=-1         dx/dy=1
      ;;                          \       y       /
      ;;                            \#####^#####/
      ;;                              \###|###/
      ;;                                \#|#/
      ;;                               ---X--->x
      ;;                                / | \
      ;;                              /       \

;;; and so ACOTAN goes from 135 degrees for dx/dy=-1, through 90 degrees for dx/dy=0
;;; to 45 degrees for dx/dy=1. So the plot of our ACOTAN is as follows (only the
;;; asterisks are in our domain):

      ;;                              angle [degrees]
      ;;                                 ^
      ;;               ACOTAN ''''-.     |
      ;;                            '* - +-135
      ;;                             | * |
      ;;                             |   *-90
      ;;                             |   | *
      ;;                             |45-+   *.
      ;;                             |   |   | '-....
      ;;                       ------+---X---+---------> dx/dy
      ;;                            -1   |   1
      ;;                                 |
      ;;

;;; In this domain and codomain we define our acotan function. As we can see from the plots
;;; of ACOTAN and ATAN, we can see that ACOTAN = - ATAN + 90 degrees.
;;; The fact that we labelled the argument axes in our plots dy/dx or dx/dy doesn't matter
;;; for the function itself: it just marks their intended usage in context of geometry.

      (defn acotan [dx-to-dy]
        (- (/ pi 2) (Math/atan dx-to-dy))))

;;;### Conversions
    (do
;;; Now we can define functions to convert slopes (dx, dy) to radians.

      ;;                      \  |dy|>|dx| and dy>0   /
      ;;                        \                   /
      ;;                          \ ACOTAN(dx/dy) /
      ;;                            \           /
      ;; |dy|<|dx| and dx<0           \   ^   /     |dy|<|dx| and dx>0
      ;;                                \ | /
      ;; ATAN(dy/dx) + 180 deg         ---X--->     ATAN(dy/dx)
      ;; and normalize to [-180,180)    / | \
      ;;                              /   |   \
      ;;                            /           \
      ;;                          /ACOTAN(dx/dy)  \
      ;;                        / +180 deg and norm.\
      ;;                      /                       \
      ;;                    /    |dy|>|dx| and dy>0     \

;;;
      (defn- -normalize-radians [radians]
        (loop [out radians]
          (if (>= out pi)
            (recur (- out (* 2 pi)))
            out)))

      (defn- -slope-to-radians [dx dy]
        (if (<= (Math/abs dy) (Math/abs dx))
          (if (> dx 0)
            (atan (/ dy dx))
            (-normalize-radians (+ (atan (/ dy dx)) pi)))
          (if (> dy 0)
            (acotan (/ dx dy))
            (-normalize-radians (+ (acotan (/ dx dy)) pi)))))))

;;;## Analytic representation (radians)
  (do

;;; To create the analytic representation of a given number of radians:
    (defn radians [num]
      {:radians num})

;;; To convert or augment any angle to the analytic representation:
    (defn in-radians [angle]
      (if (number? angle)
        #_"bare number: convert to representation"
        {:radians angle}
        #_"otherwise: augment the representation with radians"
        (let [{:keys [radians degrees slope]} angle]
          (cond
            radians angle #_"(radians already in representation: nothing to do)"
            degrees (assoc angle :radians (* radian-to-degree-coeff degrees))
            slope (let [[dx dy] slope]
                    (assoc angle :radians (-slope-to-radians dx dy)))))))

;;; To get the number of radians (which also represents the angle
;;; as bare number) from any angle:
    (defn get-radians [angle]
      (if (number? angle)
        #_"bare number: it is the radians value itself"
        angle
        #_"otherwise: get or calculate the value from representation"
        (let [{:keys [radians degrees slope]} angle]
          (cond
            radians radians #_"(radians are in representation: just get it)"
            degrees (* radian-to-degree-coeff degrees)
            slope (let [[dx dy] slope] (-slope-to-radians dx dy))))))

;;; For example,
    (examples
;;;
     (radians 3.14) => {:radians 3.14}
;;;
     (in-radians 3.14) => {:radians 3.14}
     (in-radians {:radians 3.14}) => {:radians 3.14}
     (in-radians {:degrees 90}) => {:degrees 90, :radians (/ pi 2)}
     (in-radians {:slope [-1 0]}) => {:slope [-1 0], :radians (- pi)}
;;;
     (get-radians 3.14) => 3.14
     (get-radians {:radians 3.14}) => 3.14
     (get-radians {:degrees 180}) => pi
     (get-radians {:degrees 180, :radians 3.146}) => 3.146
     (get-radians {:slope [-1 0]}) => (- pi)

     )

    )

;;;## Degrees
  (do

;;; Create:
    (defn degrees [num]
      {:degrees num})

;;; Convert/augment:
    (defn in-degrees [angle]
      (if (number? angle)
        {:degrees (* degree-to-radian-coeff angle)}
        (let [{:keys [radians degrees slope]} angle]
          (cond
            degrees angle
            radians (assoc angle :degrees (* degree-to-radian-coeff radians))
            slope (let [[dx dy] slope]
                    (assoc angle :degrees
                           (* degree-to-radian-coeff (-slope-to-radians dx dy))))))))

;;; Get numeric value:
    (defn get-degrees [angle]
      (if (number? angle)
        (* degree-to-radian-coeff angle)
        (let [{:keys [radians degrees slope]} angle]
          (cond
            degrees degrees
            radians (* degree-to-radian-coeff radians)
            slope (let [[dx dy] slope]
                    (* degree-to-radian-coeff (-slope-to-radians dx dy)))))))

;;; For example,
    (examples
;;;
     (in-degrees pi) => {:degrees 180.0} #_"- bare number is always radians!"
     (in-degrees {:degrees 180}) => {:degrees 180}
     (in-degrees {:radians pi}) => {:radians pi, :degrees 180.0}
     (in-degrees {:slope [-2 0]}) => {:slope [-2 0], :degrees -180.0}
;;;
     (get-degrees pi) => 180.0 #_"- bare number is always radians!"
     (get-degrees {:degrees 180}) => 180
     (get-degrees {:radians pi}) => 180.0
     (get-degrees {:radians 3, :degrees 180}) => 180
     (get-degrees {:slope [-2 0]}) => -180.0))

;;;## Slope
  (do

;;; Create:
    (defn slope [dx dy]
      {:slope [dx dy]})

;;; Convert/augment:
    (defn in-slope [angle]
      (if (number? angle)
        {:slope [(Math/cos angle) (Math/sin angle)]}
        (let [{:keys [radians degrees slope]} angle]
          (cond
            slope angle
            radians (assoc angle :slope [(Math/cos radians) (Math/sin radians)])
            degrees (let [radians (* radian-to-degree-coeff degrees)]
                      (assoc angle :slope [(Math/cos radians) (Math/sin radians)]))))))

;;; Get numeric values:
    (defn get-slope [angle]
      (if (number? angle)
        [(Math/cos angle) (Math/sin angle)]
        (let [{:keys [radians degrees slope]} angle]
          (cond
            slope slope
            radians [(Math/cos radians) (Math/sin radians)]
            degrees (let [radians (* radian-to-degree-coeff degrees)]
                      [(Math/cos radians) (Math/sin radians)]))))))

  (examples
;;;
   (in-slope 0) => {:slope [1.0 0.0]} #_"- bare number is always radians!"
   (in-slope {:degrees 0}) => {:degrees 0, :slope [1.0 0.0]}
   (in-slope {:radians 0}) => {:radians 0, :slope [1.0 0.0]}
   (in-slope {:slope [-2 0]}) => {:slope [-2 0]}
;;;
   (get-slope 0) => [1.0 0.0] #_"- bare number is always radians!"
   (get-slope {:degrees 0}) => [1.0 0.0]
   (get-slope {:radians 0}) => [1.0 0.0]
   (get-slope {:radians 0, :slope [2.1 0.1]}) => [2.1 0.1]
   (get-slope {:slope [-2 0]}) => [-2 0]))

;;;# Paths in general
(do
;;; Paths are lines, curves, segmented lines etc.
;;; Their main purpose in gamebase is for game objects to move
;;; along them.

;;; TODO ;;; *Napisac o tym, ze musi byc to linia ciagla.*
;;; *A dlaczego?*
;;; *No bo chcemy miec zasade, ze drobna niedokladnosc ze wzgledu na arytmetyke zmiennoprzecinkowa*
;;; *nie bedzie powodowala, ze np. sprite przeskoczy gdzies w kosmos. Jesli taki skok jest zamierzony,*
;;; *to raczej powinna go obsluzyc specyficzna logika w grze, uzywajac dwoch osobnych "paths".*

;;; They'll be maps with the |:path-type| value, specific
;;; for different kinds of paths:
  {:path-type :example-type
   ;; ...
   }
;;;

;;; Operations on will be declared as multimethods
;;; dispatching on object types.


;;;## Lengths and points

  (do

;;; We'll want to know the length of a path:
    (defmulti path-length (fn [path] (:path-type path)))

;;; Also, to calculate coordinates of the point at given length from start:
    (defmulti path-point-at-length
      (fn [path length] (:path-type path)))

;;; and from end:
    (defmulti path-point-at-length
      (fn [path length] (:path-type path))))

;;;## Angles
  (do

    (defmulti angle-at-length
      (fn [path length] (:path-type path))))

;;;## Modifications
  (do
    (defmulti translate-path (fn [path dx dy] (:path-type path))))

;;;## Precomputation

  (do

    (defmulti precompute
      (fn [path] (:path-type path)))

    (defn precomputed [path]
      (if (:precomputed? path)
        path
        (assoc
         (precompute path)
         :precomputed? true)))))

;;;# Line segments
(do
;;; A line segment is a section of a straight line
;;; extending from one point to another:

  (defn line-segment [p1 p2]
    {:path-type :line-segment
     :p1 p1
     :p2 p2})

;;; TODO !!! - zrobic to, wyliczac full-length, x-factor, y-factor.
;;; Potem w funkcjach ponizej uzywac (precomputed path)
;;; UWAGA. Jakis marker? Bo (precomputed (precomputed path)) nie powinno liczyc dwa razy.
  (defmethod precompute :line-segment [path] path)

;;; E.g.
  (examples

   (line-segment [2 3] [4 -1]) => {:path-type :line-segment
                                   :p1 [2 3]
                                   :p2 [4 -1]})

;;; The length of a line segment:

  (defmethod path-length :line-segment
    [{[x1 y1] :p1, [x2 y2] :p2}]
    (let [dx (- x2 x1), dy (- y2 y1)]
      (sqrt (+ (* dx dx) (* dy dy)))))

;;; E.g.
  (examples
   (path-length (line-segment [0 0] [3 4])) => 5.0
   (path-length (line-segment [-2 5] [1 9])) => 5.0)

;;; At length:

  (defmethod path-point-at-length :line-segment
    [{[x1 y1] :p1, [x2 y2] :p2 :as segment}, length]
    (let [dx (- x2 x1), dy (- y2 y1)
          full-length (path-length segment)
          x-factor (/ dx full-length)
          y-factor (/ dy full-length)]
      [(+ x1 (* length x-factor))
       (+ y1 (* length y-factor))]))

;;; Angle:
  (defmethod angle-at-length :line-segment
    [{[x1 y1] :p1, [x2 y2] :p2 :as segment}, length]
    (let [dx (- x2 x1), dy (- y2 y1)]
      (get-radians (slope dx dy))))

;;;  TODO !!! zaimplementowac

;;; E.g.:

  (examples
   (let [my-path (line-segment [-2 5] [1 9])]
     (path-point-at-length
      my-path
      (/ (path-length my-path) 2)))
   =>
   [-0.5 7.0])

;;; *NOTE*

;;; Nie sprawdzamy, czy nie wyjechalismy poza segment.
;;; Zrobic z tego jakas polityke?
;;; Ze np. jesli malo wyjechalismy, to powinnismy
;;; tez malo wyjechac na wyniku.
;;; - to bedzie bezpieczenstwo na rozne bledy zaokraglen.

;;; TODO

;;; Drogie rzeczy robic pre-computed!
;;; Czyli juz przy line-segment
;;; wpisywac do mapki:
;;; - length
;;; - x-factor
;;; - y-factor

;;; moze jakos opcjonalnie


;; --  MOZE ZROBIC TAKA MULTIMETODE "precompute",
;; --  KTORA DOWOLNY PATH BEDZIE WZBOGACALA O TAKIE RZECZY

;;; Mozna potem bedzie robic taka sztuczke:

;;; 1. krzywe torow zdefiniowac raz, zero-based

;;; 2. a potem w kazdym realnym kafelku, czy moze w trakcie gry
;;; w lokomotywie, mozna miec krzywa "nested"
;;;`  {:path-type :translated
;;;`   :path (tutaj "nested" krzywa)
;;;`   :dx
;;;`   :dy
;;;


  (defmethod translate-path :line-segment
    [{[x1 y1] :p1, [x2 y2] :p2 :as segment}, dx, dy]
    (assoc segment
           :p1 [(+ x1 dx) (+ y1 dy)]
           :p2 [(+ x2 dx) (+ y2 dy)]))

  )

;;;# Circle arcs
(do

;;;    positive direction:                           negative direction:
;;;
;;;                    ^                                           ^
;;;                    |                                           |
;;;               __.-----.__                                 __.-----.__
;;;           |.-'           ''-._                         .-'           ''-._
;;;     end   '-- __.-----.__     \                start  '   __.-----.__    _\|
;;;         # .-''     |     ''-.    start              # .-''     |     ''-.  ' end
;;;          #         |         \ #                     #         |         \ #
;;;         / #        |          #                     / #        |          #
;;;        /           |         # \                   /           |         # \
;;;       |            |            |                 |            |            |
;;;       |            |            |                 |            |            |
;;;  -----|------------+------------|------->    -----|------------+------------|------->
;;;       |            |            |                 |            |            |
;;;       |            |            |                 |            |            |
;;;        \           |           /                   \           |           /
;;;         \          |          /                     \          |          /
;;;          \         |         /                       \         |         /
;;;           '-..__   |   __..-'                         '-..__   |   __..-'
;;;                 '-----'                                     '-----'
;;;                    |                                           |
;;;                    |                                           |
;;;                    |                                           |

  (defn circle-arc [center radius angle-start angle-end direction]
    (assert (#{:positive :negative} direction))
    {:path-type :circle-arc
     :center center
     :radius radius
     :angle-start angle-start
     :angle-end angle-end
     :direction direction})

  (defn normalize-to-2pi [radians]
    (let [radians' (loop [r radians]
                    (if (>= r (* 2 pi))
                      (recur (- r (* 2 pi)))
                      r))
          radians'' (loop [r radians']
                      (if (< r 0)
                        (recur (+ r (* 2 pi)))
                        r))]
      radians''))

  (defmethod precompute :circle-arc
    [{[xc yc] :center, :keys [radius angle-start angle-end direction] :as path}]

    (case direction

      :positive

      (let [st (normalize-to-2pi (get-radians angle-start))
            en0 (normalize-to-2pi (get-radians angle-end))
            en (if (> en0 st) en0 (+ (* 2 pi) en0))]
        (assoc path
               :st st
               :en en
               :length (* radius (- en st))))

      :negative

      (let [en (normalize-to-2pi (get-radians angle-end))
            st0 (normalize-to-2pi (get-radians angle-start))
            st (if (> st0 en) st0 (+ (* 2 pi) st0))]
        (assoc path
               :st st
               :en en
               :length (* radius (- st en))))))

  (defmethod path-length :circle-arc [path]
    (:length (precomputed path)))

  (examples
   (path-length (circle-arc [0 0] 1 (degrees 90) (degrees 180) :positive)) => (/ pi 2)
   (path-length (circle-arc [0 0] 1 (degrees 270) (degrees 90) :positive)) => pi
   (path-length (circle-arc [0 0] 1 (degrees (- 360 35)) (degrees 10) :positive))
   ,                                                                      => (/ pi 4)
   (path-length (circle-arc [0 0] 2 (degrees (- 360 35)) (degrees 10) :positive))
   ,                                                                     => (/ pi 2))

  (defmethod path-point-at-length :circle-arc [path length]
    (let [{[xc yc] :center, :keys [radius st en direction]} (precomputed path)
          angle (case direction
                  :positive (+ st (/ length radius))
                  :negative (- st (/ length radius)))]
      [(+ xc (* radius (cos angle)))
       (+ yc (* radius (sin angle)))]))

  (defmethod angle-at-length :circle-arc [path length]
    (let [{[xc yc] :center, :keys [radius st direction]} (precomputed path)]
      (normalize-to-2pi
       (case direction
         :positive (+ st (/ length radius) (/ pi 2))
         :negative (- st (/ length radius) (/ pi 2))))))

  (defmethod translate-path :circle-arc
    [{[xc yc] :center :as arc}, dx, dy]
    (assoc arc
           :center [(+ xc dx) (+ yc dy)]))

  )

;;; TODO - na poczatku (na koncu?) dac jakis "synopsis", pokazujacy struktury danych i funkcje
;;; Moze na poczatku - i wtedy to bedzie zdefiniowana funkcja (synopsis), majaca w srodku asserty
;;; (czy tez "examples"), a na samym koncu pliku wywolamy te funkcje, zeby sie upewnic,
;;; ze wszystko tam jest prawda.


;;; A W OGOLE ZROBIC DRUKOWANIE! Nie wiem czy automatem jakims html2pdf, czy zrobic osobny rendering np. do latexa.
;;; BO MOZNA BY ZROBIC WYMUSZANE PAGE BREAKI np. przed rozdzialem, albo recznie wstawiane


