;;;;; STATUS: ON HOLD ;;;;;

(ns gamebase.ecsu)

;; private - bare identifiers to be used in macros
;; (def -id-event-target-prefix `<-event-target-prefix>)
;; (def -id-world `<world>)
;; (def -id-event `<event>)
;; (def -id-this `<this>) (declare <this>)
;; (def -id-time `<time>)
;; (def -id-entity `<entity>)
;; (def -id-component-key `<component-key>)

(defmacro component [id & body]
  (let [body' (remove #(and (seq? %) (= 'local (first %))) body)
        locals (->> body
                    (filter #(and (seq? %) (= 'local (first %))))
                    (mapcat rest))]
    `(binding [<-event-target-prefix> [:to-component ~id]]
       (let [~@locals]
         ~@body'))))

(defmacro handle-event [msg & body]
  `(let [event-target# (into <-event-target-prefix> [~msg])]
     (defmethod ecs/handle-event event-target#
       [<world> <event> <this>]
       (let [<time> (:gamebase.event-queue/time <event>)]
         ~@body))))

(defmacro my-entity []
  `(ecs/get-entity <world> <this>))

(defmacro mk-component [constructor options]
  `(~constructor <entity> <component-key> ~options))

(defmacro mk-entity [id kind components & other-data-kvs]
  (let [components'
        (->> components
             (mapcat (fn [[k v]]
                       [k `(binding [<component-key> ~k] ~v)]))
             (apply hash-map))]
    `(binding [<entity> (ecs/mk-entity ~id ~kind)]
       (assoc
        <entity>
        :gamebase.ecs/components ~components'
        ~@other-data-kvs))))


