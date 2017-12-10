(ns gamebase.layouts.sidebar-and-bottombar
  (:require
   [gamebase.events :as events]))

;; TODO:
;;  - the layout must also control canvas itself, resize it etc.
;;  - as for positioning *content* inside the layout (which depends on particular game logic)
;;    it must be passed as callbacks to game logic

(declare -setup-events update-canvas-size show-canvas)

;; This is the "constant" part of the state of the layout.
;; That is, it should be set identically for every game.
;; It will not be saved with save game.
(defonce state (atom {}))

;; API

(defn mk-layout [base-atom kvs opts]
  (reset! state (assoc opts
                       :base-atom base-atom
                       :kvs kvs))
  (-setup-events)
  (swap! base-atom update-in kvs (constantly {})))

(defn initialize []
  (let [{:keys [base-atom kvs]} @state
        cnv (js/createCanvas 600 400)]
    (.parent cnv "gamebase/canvas-holder")
    (update-canvas-size)
    (show-canvas)
    (update-canvas-size)))

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

(defn get-canvas-rectangle []
  (let [{:keys [base-atom kvs]} @state]
    (if-let [{:keys [canvas-x canvas-y canvas-width canvas-height]}
             (get-in @base-atom kvs)]
      [[canvas-x canvas-y]
       [(+ canvas-x canvas-width) (+ canvas-y canvas-height)]]
      [[0 0] [0 0]])))

;; PRIVATE

(defn position-element [element x y width height]
  (set! (.-height (.-style element)) (str height "px"))
  (set! (.-top (.-style element)) (str y "px"))
  (set! (.-left (.-style element)) (str x "px"))
  (set! (.-width (.-style element)) (str width "px")))

(defn update-canvas-size []
  (let [{:keys [base-atom kvs bottom-bar-height after-canvas-resize]} @state
        width (.-innerWidth js/window)
        height (.-innerHeight js/window)
        canvas-width (- width 200)
        canvas-height (- height bottom-bar-height)]

    (position-element (get-canvas-holder-element)
                      200 0 canvas-width canvas-height)
    (position-element (get-side-bar-element)
                      0 0 200 height)
    (position-element (get-bottom-bar-element)
                      200 (- height bottom-bar-height)
                      (- width 200) bottom-bar-height)
    (js/resizeCanvas canvas-width canvas-height)
    (swap! base-atom update-in kvs
           assoc
           :canvas-x 200
           :canvas-y 0
           :canvas-width canvas-width
           :canvas-height canvas-height)
    (after-canvas-resize)))

(defn -setup-events []
  (let [{:keys [base-atom kvs]} @state]
    (events/add-handler :window-resized
                        (fn [_] (update-canvas-size)))))

