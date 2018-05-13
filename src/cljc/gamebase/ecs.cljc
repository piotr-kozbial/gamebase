(ns gamebase.ecs
  (:require [schema.core :as s :include-macros true]))

;;;;;;;;;;;;;;;;;;;;;; P U B L I C ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
nil

;;;;; Generic schema utilities

(defn s-literal
  "schema for a single fixed value"
  [v]
  (s/pred #(= % v)))

;;;;; Event targets

(def sTarget
  "Schema for event targets (? then why the generic map?)"
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

(defn to-world []
  {::kind :to-world})

(defn to-system [system]
  {::kind :to-system
   ::system-id (::system-id system)})

(defn to-entity [entity]
  {::kind :to-entity
   ::entity-id (::entity-id entity)})

(defn to-component [component]
  {::kind :to-component
   ::entity-id (::entity-id component)
   ::component-id (::component-key component)})

;;;;; Objects

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

;;;;; Event handling

;; Return schema for `handle-event`. It may return
;; - an object: world, system, entity or component,
;; - a sequential of objects.
(def sCallHandleReturn (s/conditional
                        map? sObject
                        sequential? [sObject]))

;; Note. It is not possible to assign schema to a multimethod.
;; Schema will be controlled in the implementation
;; of do-handle-event.
;; This method (`handle-event`) is to be implemented by clients,
;; but not called directly.
;; Client should always call `do-handle-event`.
(defmulti handle-event
  (fn [target-id event total-time world object]
    (case (::kind target-id)
      ;; these instances to be handled on a global (world) level
      :to-world
      ,   [:to-world (::msg event)]
      ;; these instances to be defined by given system
      :to-system
      ,   [:to-system (::system-id target-id) (::msg event)]
      ;; these instances to be defined next to entity constructor
      :to-entity
      ,   [:to-entity (::type object) (::msg event)]
      ;; these instances to be defined by system
      ;; to which component belongs
      :to-component
      ,   [:to-component (::type object) (::msg event)])))

(declare call-handle-event ;; private
         resolve-target-id ;; private
         insert-object)    ;; private

;; Event-handling function to be called by clients.
;; Always returns world.
(s/defn do-handle-event :- sWorld
  [target-id event total-time world]
  (let [object (resolve-target-id world target-id)
        return (call-handle-event target-id event total-time world object)
        new-objects
        (if (map? return)
          [return] ;; single object - pack into vector to make it seqable
          return ;; otherwise - should be a seqable
          )]
    (reduce
     insert-object
     world
     new-objects)))

;;;;;;;;;;;;;;;;;;;;;; p r i v a t e ;;;;;;;;;;;;;;;;;;;;;;;;;;
nil

;; Scheme'd function just calling the multimethod
;; This is only because schema for a multimethod
;; is not supported.
(s/defn ^:always-validate call-handle-event :- sCallHandleReturn
  [target-id event total-time world object]
  (handle-event target-id event total-time world object))

;; helper function
(defn resolve-target-id [world target-id]
  (case (::kind target-id)
    :to-world
    ,  world
    :to-system
    ,  ((::systems world) (::system-id target-id))
    :to-entity
    ,  ((::entities world) (::entity-id target-id))
    :to-component
    ,  (let [entity ((::entities world) (::entity-id target-id))]
         ((::components entity) (::component-id target-id)))))

;; helper function
(defn insert-object [world object]
  (case (::kind object)
    :world
    ,  object ;; we replace the whole world
    :system
    ,  (assoc-in world [::systems (::system-id object)] object)
    :entity
    ,  (assoc-in world [::entities (::entity-id object)] object)
    :component
    ,  (assoc-in world [::entities (::entity-id object)
                        ::components (::component-key object)] object)))

