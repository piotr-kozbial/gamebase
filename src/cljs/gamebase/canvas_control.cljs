(ns gamebase.canvas-control
  (:require
   [gamebase.events :as events]))


(def conf (atom {}))

(defn- correct-top-left [top-left-x top-left-y]
  (let [scale ((:get-scale @conf))
        canvas-width ((:get-canvas-width @conf))
        canvas-height ((:get-canvas-height @conf))]

    [(->> top-left-x
          (min (+ 20 (- ((:get-world-width @conf)) (/ canvas-width scale))))
          (max -20))
     (->> top-left-y
          (min (+ 20 (- ((:get-world-height @conf)) (/ canvas-height scale))))
          (max -20))]))

(defn- after-canvas-resize []
  (let [{:keys [state-atom state-kvs]} @conf]
    (swap! state-atom
           update-in state-kvs
           (fn [{:keys [top-left-x top-left-y] :as st}]
             (let [[new-top-left-x new-top-left-y] (correct-top-left top-left-x top-left-y)]
               (assoc st
                      :top-left-x new-top-left-x
                      :top-left-y new-top-left-y))))))

(defn- setup-drag-event []
  (events/add-handler :canvas-mouse-dragged
                      (fn [{:keys [button x y prev-x prev-y]}]
                        (when (= button js/RIGHT)
                          (let [canvas-width ((:get-canvas-width @conf))
                                canvas-height ((:get-canvas-height @conf))
                                dx (- x prev-x)
                                dy (- y prev-y)
                                scale ((:get-scale @conf))
                                {:keys [state-atom state-kvs]} @conf]
                            (swap! state-atom
                                   update-in state-kvs
                                   (fn [{:keys [top-left-x top-left-y] :as st}]
                                     (let [[new-top-left-x new-top-left-y]
                                           ,   (correct-top-left (- top-left-x (/ dx scale))
                                                                 (- top-left-y (/ dy scale)))]
                                       (assoc st :top-left-x new-top-left-x
                                              :top-left-y new-top-left-y)))))))))

;; API

(defn setup [config]
  (reset! conf config)
  (setup-drag-event))

(defn mk-canvas-resize-handler [_]
  (fn [] (after-canvas-resize)))

(defn set-scale [scale]
  ;; (swap! makstycoon.global/app-state assoc :scale scale) ;; TODO - better
  (after-canvas-resize))

(defn reset-transformation []
  (let [canvas-width ((:get-canvas-width @conf))
        canvas-height ((:get-canvas-height @conf))
        scale_ ((:get-scale @conf))
        {:keys [state-atom state-kvs]} @conf
        {:keys [top-left-x top-left-y]} (get-in @state-atom state-kvs)]
    (js/resetMatrix)
    (js/scale scale_)
    (js/translate (- top-left-x) (- top-left-y))))

(defn get-top-left-in-world-coords []
  (let [{:keys [state-atom state-kvs]} @conf
        {:keys [top-left-x top-left-y]} (get-in @state-atom state-kvs)
        canvas-width ((:get-canvas-width @conf))
        canvas-height ((:get-canvas-height @conf))
        scale_ ((:get-scale @conf))]
    [top-left-x top-left-y (/ canvas-width scale_) (/ canvas-height scale_)]))
