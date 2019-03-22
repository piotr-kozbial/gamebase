(ns gamebase.systems.drawing
  (:require
   [gamebase.ecs :as ecs]
   [gamebase.resources :as resources]
   [gamebase.layers :as layers]
   [gamebase.geometry :as g]
   ;;[app.tiles :as tiles]
   [schema.core :as s :include-macros true]))

;; utils

(defn -put-image [img x y w h dst-x dst-y]
  (js/push)
  (js/scale 1 -1)
  (js/image img
            ;; destination
            dst-x (- (* -1 dst-y) h) w h
            ;; source
            x y w h)
  (js/pop))

;; TILE EXTRA

(defmulti draw-tile-extra (fn [tile-id tx ty tile-info] tile-id) :default nil)

(defmethod draw-tile-extra nil
  [tile-id tile-info tx ty]
  false)

(do ;; SYSTEM

  (defn mk-system []
    (ecs/mk-system ::drawing))

  (def to-system
    (ecs/to-system ::drawing))

  (defn- -get-layer [world layer-key]
    (->> (:layers world)
         (filter #(= (first %) layer-key))
         (first)
         (second)))

  (def s-draw-context
    {:min-x s/Int
     :max-x s/Int
     :min-y s/Int
     :max-y s/Int
     :mouse-x s/Int
     :mouse-y s/Int})

  (def s-tile-context
    {:tile-width s/Any
     :tile-height s/Any
     :world-width-in-tiles s/Any
     :world-height-in-tiles s/Any
     :tileset-rendering-map s/Any
     :tileset-map {:kafelki s/Any}})

  (defn- -draw-layer [world layer {:keys [min-x max-x min-y max-y] :as context} draw-extra?]

    (s/validate s-draw-context context)
    (s/validate s-tile-context (:tile-context world))

    (let [{:keys [tile-width tile-height
                  world-width-in-tiles
                  world-height-in-tiles] :as ctx} (:tile-context world)
          tx-min (max 0 (- (int (/ min-x tile-width)) 1))
          tx-max (min (+ (int (/ max-x tile-width)) 2) (dec world-width-in-tiles))
          ty-min (max 0 (- (int (/ min-y tile-width)) 1))
          ty-max (min (+ (int (/ max-y tile-height)) 2) (dec world-height-in-tiles))
          tx-range (range tx-min (inc tx-max))
          ty-range (range ty-min (inc ty-max))]
      (doall
       (for [tx tx-range, ty ty-range]
         (let [[_ tile-number :as tl] (layers/get-tile-from-layer layer tx ty)
               {:keys [img x y w h] :as inf}
               ,  (layers/get-rendering-information-for-tile ctx tl)]
           (when tl

             (-put-image (resources/get-resource img)
                         x y w h (* tile-width tx) (* tile-height ty))

             ;;(when draw-extra?
             ;;  (let [tile-data (tiles/tiles-by-number tile-number)]
             ;;    (some #(draw-tile-extra % tx ty tile-data) (:ids tile-data))))
             ))))


      ))

  ;; (defmethod ecs/handle-event [:to-system ::drawing ::draw]
  ;;   [world {:keys [context] :as event} system]

  ;;   ;; TODO!
  ;;   ;; To przez chwile moze byc tak (tylko zadbac, zeby tlo bylo *pod* fg).
  ;;   ;; Ale w przyszlosci ma byc tak, ze komponenty (razem z ich entities)
  ;;   ;; beda na roznych layerach i trzeba bedzie przeplatac rysowanie
  ;;   ;; layerow z rysowaniem komponentow nad tymi layerami.

  ;;   ;; draw layers
  ;;   ;;(-draw-layer world (-get-layer world :background) context false)
  ;;   (-draw-layer world (-get-layer world :foreground) context true)

  ;;   ;; draw components
  ;;   (-> world
  ;;       (#(ecs/pass-event-through-all
  ;;          % event (ecs/all-components-of-system % system))))

  ;;   )

  (defn draw-all [world context]
    ;; TODO!
    ;; To przez chwile moze byc tak (tylko zadbac, zeby tlo bylo *pod* fg).
    ;; Ale w przyszlosci ma byc tak, ze komponenty (razem z ich entities)
    ;; beda na roznych layerach i trzeba bedzie przeplatac rysowanie
    ;; layerow z rysowaniem komponentow nad tymi layerami.

    ;; draw layers
    ;;(-draw-layer world (-get-layer world :background) context false)
    (-draw-layer world (-get-layer world :foreground) context true)

    (dorun
     (ecs/pass-event-through-all
      world (ecs/mk-event to-system ::draw 0)
      (ecs/all-components-of-system world
                                    (::drawing (::ecs/systems world))))))

 


  (defmethod ecs/handle-event [:to-system ::drawing :update] [world event system]
    (let [world'(ecs/pass-event-through-all world event (ecs/all-components-of-system world system))]
      world')))

(do ;; COMPONENT: static image

  (defn mk-static-image-component
    [entity-or-id key {:keys [point-kvs angle-kvs center resource-name-kvs]}]
    (assoc
     (ecs/mk-component ::drawing entity-or-id key ::static-image)
     :point-kvs point-kvs
     :angle-kvs angle-kvs
     :center center
     :resource-name-kvs resource-name-kvs))

  (defmethod ecs/handle-event [:to-component ::static-image :update]
    [world event component]
    ;; (print (str "drawing/static-image: update, " (pr-str component)))

    component)

  (defmethod ecs/handle-event [:to-component ::static-image ::draw]
    [world event {:keys [point-kvs angle-kvs center resource-name-kvs] :as component}]
    (let [entity (ecs/get-entity world component)
          [point-x point-y] (get-in entity point-kvs)
          [center-x center-y] center
          angle (get-in entity angle-kvs)]
      (when-let [img (resources/get-resource (get-in entity resource-name-kvs))]
        (js/push)
        (js/translate point-x point-y)
        (js/rotate angle)
        ;; flip vertical, since images are placed by js/images
        ;; in screen orientation (y increasing downwards),
        ;; while we are using standard coordinate system
        (js/scale 1 -1)
        (js/image img (- center-x) (- center-y))
        (js/pop)))
    component))

(do ;; COMPONENT: path

  ;; draws a path as defined in gamebase.geometry

  (defn mk-path-component
    [entity-or-id key {:keys [path-kvs color]}]
    (assoc
     (ecs/mk-component ::drawing entity-or-id key ::path)
     :path-kvs path-kvs
     :color color))

  (defmethod ecs/handle-event [:to-component ::path :update]
    [world event component]
    nil)

  (defmethod ecs/handle-event [:to-component ::path ::draw]
    [world event {:keys [path-kvs] :as component}]
    (let [entity (ecs/get-entity world component)]
      (when-let [path (get-in entity path-kvs)]
        (let [len (g/path-length path)
              n (int (/ len 5))
              d (/ len n)]
          (js/push)
          (js/stroke (js/color (:color component)))
          (doseq [i (range (inc n))]
            (apply js/point (g/path-point-at-length path (* i d))))
          (js/pop)))) nil))
