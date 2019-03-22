(ns gamebase.layouts.fullscreen
  (:require
   [gamebase.events :as events]))


(defn mk-html []
  [:div {}
   [:div {:id "gamebase/canvas-holder"
          :style {:position "absolute"
                  ;; :backgroundColor "#888888"
                  }}]])

(declare -setup-events update-canvas-size show-canvas)

;; This is the "constant" part of the state of the layout.
;; That is, it should be set identically for every game.
;; It will not be saved with save game.
(defonce state (atom {}))

;; API

(defn initialize [base-atom kvs opts]
  (reset! state (assoc opts
                       :base-atom base-atom
                       :kvs kvs))
  (-setup-events)
  (swap! base-atom update-in kvs (constantly {}))
  (let [cnv (js/createCanvas 600 400)]
    (.parent cnv "gamebase/canvas-holder")
    (update-canvas-size)
    (show-canvas)
    (update-canvas-size)))

(defn get-canvas-holder-element []
  (.getElementById js/document "gamebase/canvas-holder"))

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

(defn get-canvas-size []
  (let [{:keys [base-atom kvs]} @state
        {:keys [canvas-width canvas-height]}
        ,   (get-in @base-atom kvs)]
    [canvas-width canvas-height]))

;; PRIVATE

(defn position-element [element x y width height]
  (set! (.-height (.-style element)) (str height "px"))
  (set! (.-top (.-style element)) (str y "px"))
  (set! (.-left (.-style element)) (str x "px"))
  (set! (.-width (.-style element)) (str width "px")))

(defn update-canvas-size []
  (let [{:keys [base-atom kvs bottom-bar-height side-bar-width after-canvas-resize]} @state
        width (.-innerWidth js/window)
        height (.-innerHeight js/window)]

    (position-element (get-canvas-holder-element) 0 0 width height)

    (js/resizeCanvas width height)

    (swap! state assoc
           :screen-width width
           :screen-height height
           :canvas-width width
           :canvas-height height)

    (swap! base-atom update-in kvs
           (fn [s] (assoc s
                         :canvas-x 0
                         :canvas-y 0
                         :canvas-width width
                         :canvas-height height)))

    (after-canvas-resize)))

(defn -setup-events []
  (events/add-handler :window-resized
                      (fn [_] (update-canvas-size))))
