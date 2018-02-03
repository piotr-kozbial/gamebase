(ns gamebase.ecs
  (:require [schema.core :as s :include-macros true]))

;; helpers
(defn s-literal [v]
  (s/pred #(= % v)))
;; /helpers

(def sTarget
  (s/conditional
   #(= (::kind %) :to-world)
   ,   {s/Any s/Any}
   #(= (::kind %) :to-system)
   ,   {::system-id s/Any
        s/Any s/Any}
   #(= (::kind %) :to-entity)
   ,   {::entity-id s/Any
        s/Any s/Any}
   #(= (::kind %) :to-component)
   ,   {::entity-id s/Any
        ::component-key s/Any
        s/Any s/Any}))

(def sComponent
  {::kind (s-literal :component)
   ::system-id s/Any
   ::entity-id s/Any
   ::component-key s/Any
   s/Any s/Any})

(def sEntity
  {::kind (s-literal :entity)
   ::entity-id s/Any
   ::components {s/Any sComponent}
   s/Any s/Any})

(def sSystem
  {::kind (s-literal :system)
   ::system-id s/Any
   s/Any s/Any})

(def sWorld
  {::kind (s-literal :world)
   ::systems {s/Any sSystem}
   ::entities {s/Any sEntity}
   s/Any s/Any})

(def sObject
  (s/conditional
   #(= (::kind %) :world) sWorld
   #(= (::kind %) :system) sSystem
   #(= (::kind %) :entity) sEntity
   #(= (::kind %) :component) sComponent))

(comment ; examples
  ;; sWorld
  (s/validate sWorld {::kind :world
                      ::systems {}
                      ::entities {}})

  ;; sSystem
  (s/validate sSystem {::kind :system
                       ::system-id "my system"})

  ;; sEntity
  (s/validate sEntity {::kind :entity
                       ::entity-id 10
                       ::components {}})

  ;; sComponent
  (s/validate sComponent {::kind :component
                          ::entity-id 10
                          ::component-key "comp1"
                          ::system-id "my system"})

  )





;; ;;;;; Spec helpers

;; (def anything (constantly true))

;; ;;;;; Objects

;; (s/def ::system-id anything)
;; (s/def ::entity-id anything)
;; (s/def ::component-id anything)

;; (s/def ::kind keyword?)

;; (defmulti object-spec ::kind)
;; (s/def ::object (s/multi-spec object-spec ::kind))
;; (defmethod object-spec :world [_]
;;   (s/keys :req [::kind ::systems ::entities]))
;; (defmethod object-spec :system [_]
;;   (s/keys :req [::kind ::system-id]))
;; (defmethod object-spec :entity [_]
;;   (s/keys :req [::kind ::entity-id ::components]))
;; (defmethod object-spec :component [_]
;;   (s/keys :req [::kind ::component-id ::entity-id ::system-id]))

;; (s/def ::system (s/and ::object #(= (::kind %) :system)))
;; (s/def ::systems (s/map-of ::system-id ::system))

;; (s/def ::entity (s/and ::object #(= (::kind %) :entity)))
;; (s/def ::entities (s/map-of ::entity-id ::entity))

;; (s/def ::component (s/and ::object #(= (::kind %) :component)))
;; (s/def ::components (s/map-of ::component-id ::component))

;; ;;;;; Target identification for an event

;; (defmulti target-id-spec ::kind)
;; (s/def ::target-id (s/multi-spec target-id-spec ::kind))
;; (defmethod target-id-spec :to-system [_]
;;   (s/keys :req [::system-id]))
;; (defmethod target-id-spec :to-entity [_]
;;   (s/keys :req [::entity-id]))
;; (defmethod target-id-spec :to-component [_]
;;   (s/keys :req [::entity-id ::component-id]))

;; ;;;;; Event handling
;; ;; RETURN VALUE may be an updated object or a collection of objects, including world etc.
;; ;; Everything will be inserted under appropriate id's

;; (defmulti handle-event
;;   (fn [target-id event total-time world object]
;;     (case (::kind target-id)
;;       ;; these instances to be defined by given system
;;       :to-system [:to-system (::system-id target-id) (::msg event)]
;;       ;; these instances to be defined next to entity constructor
;;       :to-entity [:to-entity (::type object) (::msg event)]
;;       ;; these instances to be defined by system to which component belongs
;;       :to-component [:to-component (::type object) (::msg event)])))


