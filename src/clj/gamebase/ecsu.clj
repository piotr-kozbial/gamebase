(ns gamebase.ecsu
;;  (:require
;;   [gamebase.ecs :as ecs]
;;   [gamebase.event-queue :as eq]
;;   
;;   )
  )

;; private - bare identifiers to be used in macros
(def -id-event-target-prefix '<-event-target-prefix>)
(def -id-world '<world>)
(def -id-event '<event>)
(def -id-this '<this>) (declare <this>)
(def -id-time '<time>)
(def -id-entity '<entity>)
(def -id-component-key '<component-key>)

(defmacro component [id & body]
  (let [body' (remove #(and (seq? %) (= 'local (first %))) body)
        locals (->> body
                    (filter #(and (seq? %) (= 'local (first %))))
                    (mapcat rest))]
    `(let [~-id-event-target-prefix [:to-component ~id]
           ~@locals]
       ~@body')))

(defmacro handle-event [msg & body]
  `(let [event-target# (into ~-id-event-target-prefix [~msg])]
     (defmethod ecs/handle-event event-target#
       [~-id-world ~-id-event ~-id-this]
       (let [~-id-time (:gamebase.event-queue/time ~-id-event)]
         ~@body))))

(defmacro my-entity []
  (let [id-world '<world>
        id-this '<this>]
    `(ecs/get-entity ~id-world ~id-this)))

(defmacro mk-component [constructor options]
  `(~constructor ~-id-entity ~-id-component-key ~options))

(defmacro mk-entity [id kind components & other-data-kvs]
  (let [components'
        (->> components
             (mapcat (fn [[k v]] [k `(let [~-id-component-key ~k] ~v)]))
             (apply hash-map))]
    `(let [~-id-entity (ecs/mk-entity ~id ~kind)]
       (assoc
         ~-id-entity
         :gamebase.ecs/components ~components'
         ~@other-data-kvs
         ))))




