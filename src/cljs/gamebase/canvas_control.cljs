(ns gamebase.canvas-control
  (:require [gamebase.events :as events]
            [gamebase.ecs :as ecs]
            [gamebase.projection :as proj]
            [schema.core :as s :include-macros true]))

(defonce conf (atom nil))

(def s-conf
  {:state-atom s/Any,:state-kvs s/Any
   (s/optional-key :advance) s/Any
   :draw s/Any
   :overlay-draw s/Any
   :get-canvas-size s/Any
   :get-world-size s/Any})

(defn get-canvas-to-world-converters []
  (when-let [{:keys [state-atom state-kvs get-canvas-size]} @conf]
    (let [{:keys [scale-factor translation-x translation-y]}
          ,    (get-in @state-atom state-kvs)
          ;; IMPORTANT!!! We must make translations integers, otherwise
          ;; there will be unwanted artifacts (lines between tiles).
          t-x (int translation-x)
          t-y (int translation-y)]
      [#(int (/ (- % t-x) scale-factor))
       #(int (/ (- % t-y) (- scale-factor)))])))

(defn- draw []
  ;; canvas clear and setup
  (js/clear)
  (js/noSmooth)
  ;; outermost scaling and translation
  (when @conf

    (s/validate s-conf @conf)

    ;; client advance
    (when-let [advance (:advance @conf)]
      (advance))

    (let [{:keys [state-atom state-kvs get-canvas-size]} @conf]
      (let [{:keys [scale-factor translation-x translation-y]}
            ,    (get-in @state-atom state-kvs)
            ;; IMPORTANT!!! We must make translations integers, otherwise
            ;; there will be unwanted artifacts (lines between tiles).
            t-x (int translation-x)
            t-y (int translation-y)]
        (js/translate t-x t-y)
        (js/scale scale-factor)
        ;; flip vertical - this allowws the client draw function
        ;; to use the standard coordinate system (y points upwards)
        (js/scale 1 -1)

        ;; We keep use the integer translations here as well
        (let [rev-x #(/ (- % t-x) scale-factor)
              rev-y #(/ (- % t-y) (- scale-factor))
              [wc hc] (get-canvas-size)
              client-context {:min-x (int (rev-x 0))
                              :max-x (int (rev-x wc))
                              :min-y (int (rev-y hc)) ;; because of negative y scale, hc is min and 0 is max
                              :max-y (int (rev-y 0))
                              :mouse-x (int (rev-x js/mouseX))
                              :mouse-y (int (rev-y js/mouseY))}]
          ;; client draw
          ((:draw @conf) client-context)
          ;; overlay draw
          (when-let [overlay-draw (:overlay-draw @conf)]
            (js/resetMatrix)
            (overlay-draw wc hc)))))))

(declare setup-drag-event)

;; public API
(defn initialize [{:keys [state-atom state-kvs] :as config}]

  (s/validate s-conf config)

  (reset! conf config)
  (swap! state-atom assoc-in state-kvs
         {:scale-factor 1
          :translation-x 100.0
          :translation-y 300.0})
  (setup-drag-event)
  (events/add-handler :draw #'draw))

(defn- make-proj-conf []
  (let [{:keys [state-atom state-kvs get-canvas-size]} @conf
        {:keys [scale-factor
                translation-x
                translation-y]} (get-in @state-atom state-kvs)
        [wc hc] (get-canvas-size)]

    (swap! state-atom update-in state-kvs
           (fn [state]
             (assoc state
                    :canvas-width wc
                    :canvas-height hc)))

    (proj/projection-config
     scale-factor
     translation-x
     translation-y
     wc
     hc)))

(defn -get-state []
  (when-let [{:keys [state-atom state-kvs]} @conf]
    (get-in @state-atom state-kvs)))

(defn get-scale []
  (when-let [{:keys [scale-factor]} (-get-state)]
    scale-factor))

(defn get-canvas-size []
  (when-let [{:keys [canvas-width canvas-height]} (-get-state)]
    [canvas-width canvas-height]))

(declare readjust)

;; public API
(defn set-scale
  "set scale in such a way that the center point of the viewport
  keeps the same world point"

  [scale-factor]

  (let [{:keys [state-atom state-kvs]} @conf
        proj-conf (make-proj-conf)
        proj-conf' (assoc proj-conf
                          :k scale-factor
                          :tx 0
                          :ty 0)
        ;; world point we want to keep at view center
        Wc (proj/world-point proj-conf (proj/Vc proj-conf))
        ;; view coords of the point after just scaling
        [x y] (proj/view-coords proj-conf' Wc)
        ;; view coords of view center (after scaling)
        [x' y'] (proj/view-coords proj-conf' (proj/Vc proj-conf'))
        ;; translation we have to do to match the above
        tr-x (- x' x)
        tr-y (- y' y)]
    (swap! state-atom update-in state-kvs
           (fn [s] (assoc s
                         :scale-factor scale-factor
                         :translation-x tr-x
                         :translation-y tr-y))))
  (readjust)
  nil)

(defn center-on
  "set translation in such a way that the center point
   of the viewport matches given world point"

  [[_ world-x world-y :as world-p]]

  (let [world-p' (proj/world-point [world-x (- world-y)])
        {:keys [state-atom state-kvs]} @conf
        proj-conf (make-proj-conf)
        proj-conf0 (assoc proj-conf
                          :tx 0
                          :ty 0)
        ;;;_ (println (str "got" (pr-str world-p')))
        ;; view coords of the given world point
        [xw yw] (proj/view-coords proj-conf0 world-p')
        ;;; _ (println [xw yw])
        ;;; _ (println proj-conf)
        ;; view coords of view center
        [xc yc] (proj/view-coords proj-conf0 (proj/Vc proj-conf0))
        ;;_ (println [xc yc])
        ;; translation we have to do to match the above
        tr-x (- xc xw)
        tr-y (- yc yw)]
    (swap! state-atom update-in state-kvs
           (fn [s] (assoc s
                         :translation-x tr-x
                         :translation-y tr-y))))
  nil
)

(def MARGIN 100)

;; TODO
;; this will need to use :get-world-size from conf
;; this will be called in setup-drag-event, set-scale etc.
(defn readjust
  "fix translation after external change
  (such as canvas resize by layout or game state reloaded)
  so that some of the world is visible at least"
  []

  ;; (.log js/console "-----------------------------------")
  ;; (.log js/console "readjust now")

  (let [{:keys [state-atom state-kvs get-world-size]} @conf
        {:keys [translation-x translation-y] :as state} (get-in @state-atom state-kvs)
        {:keys [wc hc] :as proj-conf} (make-proj-conf)

        wZ (proj/world-point [0 0])

        [ww wh] (get-world-size)
        wM (proj/world-point [ww (- wh)])

        [xZ yZ] (proj/view-coords proj-conf wZ)
        [xM yM] (proj/view-coords proj-conf wM)

        ]
    ;; (.log js/console (str "WORLD SIZE" (pr-str (get-world-size))))
    ;; (.log js/console (pr-str state))
    ;; (.log js/console (pr-str proj-conf))
    ;; (.log js/console (str "wZ = " (pr-str wZ)))
    ;; (.log js/console (str "wM = " (pr-str wM)))
    ;; (.log js/console (str "[xZ yZ] = " (pr-str [xZ yZ])))
    ;; (.log js/console (str "[xM yM] = " (pr-str [xM yM])))


    (when (> xZ (- wc MARGIN))
      ;; (.log js/console "X przekracza na prawo!")
      (swap! state-atom update-in state-kvs
             (fn [state]
               (assoc state :translation-x (- (:translation-x state) (- xZ wc) MARGIN)))))

    (when (< xM MARGIN)
      ;; (.log js/console "X przekracza na lewo!")
      (swap! state-atom update-in state-kvs
             (fn [state]
               (assoc state :translation-x (+ (:translation-x state) (- MARGIN xM))))))

    (when (> yM (- hc MARGIN))
      ;; (.log js/console "Y przekracza w dol!")
      (swap! state-atom update-in state-kvs
             (fn [state]
               (assoc state :translation-y (- (:translation-y state) (+ yM MARGIN (- hc))))))

      )

    (when (< yZ MARGIN)
      ;; (.log js/console "Y przekracza w gore!")
      (swap! state-atom update-in state-kvs
             (fn [state]
               (assoc state :translation-y (+ (:translation-y state) (- MARGIN yZ))))))))

(defn- setup-drag-event []
  (events/add-handler
   :canvas-mouse-dragged
   (fn [{:keys [button x y prev-x prev-y]}]
     (when true; (= button js/RIGHT)
       (let [{:keys [state-atom state-kvs]} @conf
             dx (- x prev-x)
             dy (- y prev-y)]
         (swap! state-atom update-in state-kvs
                (fn [{:keys [translation-x translation-y] :as s}]
                  (assoc s
                         :translation-x (+ translation-x dx)
                         :translation-y (+ translation-y dy))))
         (readjust))))))

