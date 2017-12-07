(ns gamebase.layouts.sidebar-and-bottombar
  (:require [makstycoon.globevents :as gevents]))

;; TODO:
;;  - the layout must also control canvas itself, resize it etc.
;;  - as for positioning *content* inside the layout (which depends on particular game logic)
;;    it must be passed as callbacks to game logic

(declare -setup-events update-canvas-size show-canvas)

;; API

(defn mk-layout [options]
  (let [layout (atom {:options options})]
    (-setup-events layout)
    layout))

(defn initialize [layout]

  (let [cnv (js/createCanvas 600 400)]
    (.parent cnv "gamebase/canvas-holder")
    (update-canvas-size layout)
    (show-canvas)
    (update-canvas-size layout)))

(defn get-canvas-holder-element []
  (.getElementById js/document "gamebase/canvas-holder"))
(defn get-bottom-bar-element []
  (.getElementById js/document "gamebase/bottom-bar"))
(defn get-side-bar-element []
  (.getElementById js/document "gamebase/side-bar"))
(defn get-fullpage-element []
  (.getElementById js/document "gamebase/fullpage"))
(defn get-splash-element []
  (.getElementById js/document "gamebase/splash"))

(defn hide-splash []
  (set! (.-style (get-splash-element))
        "position:absolute; z-index:100; width:100%; height:100%; opacity:0; transition: opacity 2s")
  (.setTimeout js/window #(do (set! (.-style (get-splash-element)) "display:none")) 2000))

(defn show-canvas []
  (let [canvas-holder (get-canvas-holder-element)]
    (set! (.-display (.-style canvas-holder)) "block")))
(defn hide-canvas []
  (let [canvas-holder (get-canvas-holder-element)]
    (set! (.-display (.-style canvas-holder)) "none")))

(defn get-canvas-rectangle [layout]
  (if-let [{:keys [canvas-x canvas-y canvas-width canvas-height]}
           @layout]
    [[canvas-x canvas-y]
     [(+ canvas-x canvas-width) (+ canvas-y canvas-height)]]
    [[0 0] [0 0]]))

;; PRIVATE

(defn position-element [element x y width height]
  (set! (.-height (.-style element)) (str height "px"))
  (set! (.-top (.-style element)) (str y "px"))
  (set! (.-left (.-style element)) (str x "px"))
  (set! (.-width (.-style element)) (str width "px")))

(defn update-canvas-size [layout]
  (let [{:keys [bottom-bar-height
                after-canvas-resize]} (:options @layout)
        width (.-innerWidth js/window)
        height (.-innerHeight js/window)
        canvas-width (- width 200)
        canvas-height (- height bottom-bar-height)]
    (.log js/console "update canvas size")
    (position-element (get-canvas-holder-element)
                      200 0 canvas-width canvas-height)
    (position-element (get-side-bar-element)
                      0 0 200 height)
    (position-element (get-bottom-bar-element)
                      200 (- height bottom-bar-height)
                      (- width 200) bottom-bar-height)
    (js/resizeCanvas canvas-width canvas-height)
    (swap! layout assoc
           :canvas-x 200
           :canvas-y 0
           :canvas-width canvas-width
           :canvas-height canvas-height)
    (after-canvas-resize)

    ;; (swap! global/app-state
    ;;        (fn [{:keys [top-left-x top-left-y] :as st}]
    ;;          (let [
    ;;                [new-top-left-x new-top-left-y]
    ;;                ,   (correct-top-left top-left-x
    ;;                                      top-left-y)]
    ;;            (assoc st :top-left-x new-top-left-x
    ;;                   :top-left-y new-top-left-y))))

    ))

(defn -setup-events [layout]
  (swap!
   gevents/resize-handlers
   conj
   #(update-canvas-size layout)))
