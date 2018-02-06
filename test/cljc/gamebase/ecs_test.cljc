(ns gamebase.ecs-test
  (:require [clojure.test :refer :all]
            [schema.core :as s :include-macros true]
            [clojure.pprint :refer [pprint]]
            [gamebase.ecs :as ecs]))

(do ;; data structures

  ;; target

  (deftest target-to-world
    (is (s/validate ecs/sTarget {::ecs/kind :to-world})))

  (deftest target-to-system
    (is (s/validate ecs/sTarget {::ecs/kind :to-system
                                 ::ecs/system-id "my system"})))

  (deftest target-to-entity
    (is (s/validate ecs/sTarget {::ecs/kind :to-entity
                                 ::ecs/entity-id 10})))

  (deftest target-to-component
    (is (s/validate ecs/sTarget {::ecs/kind :to-component
                                 ::ecs/entity-id 10
                                 ::ecs/component-key "comp1"})))

  ;; components

  (deftest component
    (let [obj {::ecs/kind :component
               ::ecs/system-id "my system"
               ::ecs/entity-id 10
               ::ecs/component-key "comp1"}]
      (is (s/validate ecs/sComponent obj))
      (is (s/validate ecs/sObject obj))))

  (deftest entity
    (let [obj {::ecs/kind :entity
               ::ecs/entity-id 10
               ::ecs/components {}}]
      (is (s/validate ecs/sEntity obj))
      (is (s/validate ecs/sObject obj))))

  (deftest system
    (let [obj {::ecs/kind :system
               ::ecs/system-id "my system"}]
      (is (s/validate ecs/sSystem obj))
      (is (s/validate ecs/sObject obj))))

  (deftest world
    (let [obj {::ecs/kind :world
               ::ecs/systems {}
               ::ecs/entities {}}]
      (is (s/validate ecs/sWorld obj))
      (is (s/validate ecs/sObject obj)))
    (let [obj {::ecs/kind :world
               ::ecs/systems
               {"my system" {::ecs/kind :system
                             ::ecs/system-id "my system"}}
               ::ecs/entities
               {1 {::ecs/kind :entity
                   ::ecs/entity-id 1
                   ::ecs/components {}}
                2 {::ecs/kind :entity
                   ::ecs/entity-id 2
                   ::ecs/components
                   {"comp1" {::ecs/kind :component
                             ::ecs/system-id "my system"
                             ::ecs/entity-id 2
                             ::ecs/component-key "comp1"}}}}}]
      (is (s/validate ecs/sWorld obj))
      (is (s/validate ecs/sObject obj)))))

;; (s/set-fn-validation! true)

(do ;; event handling : targets

  (def world1
    {::ecs/kind :world
     ::ecs/systems
     {"my system" {::ecs/kind :system
                   ::ecs/system-id "my system"}}
     ::ecs/entities
     {1 {::ecs/kind :entity
         ::ecs/entity-id 1
         ::ecs/type ::one-kind
         ::ecs/components {}}
      2 {::ecs/kind :entity
         ::ecs/entity-id 2
         ::ecs/type ::another-kind
         ::ecs/components
         {"comp1" {::ecs/kind :component
                   ::ecs/type ::component-a
                   ::ecs/system-id "my system"
                   ::ecs/entity-id 2
                   ::ecs/component-key "comp1"}}}}})

  (defmethod ecs/handle-event [:to-world ::test-event-to-world]
    [_ _ _ world _]
    (assoc world :a :b))

  (defmethod ecs/handle-event [:to-system "my system" ::test-event-to-system]
    [_ _ _ world _]
    (assoc world :a :sys))

  (defmethod ecs/handle-event [:to-entity ::one-kind ::test-event-to-entity]
    [_ _ _ world _]
    (assoc world :a :E))

  (defmethod ecs/handle-event [:to-component ::component-a ::test-event-to-comp]
    [_ _ _ world _]
    (assoc world :a "comp"))

  (deftest to-world
    (is (= (ecs/do-handle-event {::ecs/kind :to-world}
                                {::ecs/msg ::test-event-to-world}
                                0
                                world1)
           (assoc world1 :a :b))))

  (deftest to-system
    (is (= (ecs/do-handle-event {::ecs/kind :to-system
                                 ::ecs/system-id "my system"}
                                {::ecs/msg ::test-event-to-system}
                                0
                                world1)
           (assoc world1 :a :sys))))

  (deftest to-entity
    (is (= (ecs/do-handle-event {::ecs/kind :to-entity
                                 ::ecs/entity-id 1}
                                {::ecs/msg ::test-event-to-entity}
                                0
                                world1)
           (assoc world1 :a :E))))

  (deftest to-component
    (is (= (ecs/do-handle-event {::ecs/kind :to-component
                                 ::ecs/entity-id 2
                                 ::ecs/component-id "comp1"}
                                {::ecs/msg ::test-event-to-comp}
                                0
                                world1)
           (assoc world1 :a "comp")))))

(do ;; event handling : returns

  (def world2
    {::ecs/kind :world
     ::ecs/systems
     {"my system" {::ecs/kind :system
                   ::ecs/system-id "my system"}}
     ::ecs/entities
     {1 {::ecs/kind :entity
         ::ecs/entity-id 1
         ::ecs/type ::one-kind
         ::ecs/components {}}
      2 {::ecs/kind :entity
         ::ecs/entity-id 2
         ::ecs/type ::another-kind
         ::ecs/components
         {"comp1" {::ecs/kind :component
                   ::ecs/type ::component-a
                   ::ecs/system-id "my system"
                   ::ecs/entity-id 2
                   ::ecs/component-key "comp1"}}}}})

  (defmethod ecs/handle-event [:to-system "my system" ::test-event-return-world]
    [_ _ _ world _]
    (assoc world :whole :world))

  (defmethod ecs/handle-event [:to-system "my system" ::test-event-return-system]
    [_ _ _ world system]
    (assoc system :changed :system))

  (defmethod ecs/handle-event [:to-entity ::one-kind ::test-event-return-entity]
    [_ _ _ world entity]
    (assoc entity :modified :entity))

  (defmethod ecs/handle-event [:to-entity ::another-kind ::test-event-return-component]
    [_ _ _ world entity]
    (assoc ((::ecs/components entity) "comp1") :mod "component"))

  (deftest return-world
    (is (= (ecs/do-handle-event {::ecs/kind :to-system
                                 ::ecs/system-id "my system"}
                                {::ecs/msg ::test-event-return-world}
                                0
                                world2)
           (assoc world2 :whole :world))))


  (deftest return-system
    (is (= (ecs/do-handle-event {::ecs/kind :to-system
                                 ::ecs/system-id "my system"}
                                {::ecs/msg ::test-event-return-system}
                                0
                                world2)
           (assoc-in world2 [::ecs/systems "my system" :changed] :system))))

  (deftest return-entity
    (is (= (ecs/do-handle-event {::ecs/kind :to-entity
                                 ::ecs/entity-id 1}
                                {::ecs/msg ::test-event-return-entity}
                                0
                                world2)
           (assoc-in world2 [::ecs/entities 1 :modified] :entity))))

  (deftest return-component
    (is (= (ecs/do-handle-event {::ecs/kind :to-entity
                                 ::ecs/entity-id 2}
                                {::ecs/msg ::test-event-return-component}
                                0
                                world2)
           (assoc-in world2 [::ecs/entities 2 ::ecs/components "comp1" :mod] "component"))))

  (defmethod ecs/handle-event [:to-entity ::one-kind ::test-event-return-multi]
    [_ _ _ world entity]
    [{::ecs/kind :system
      ::ecs/system-id "my system"
      :system :modification}
     (assoc entity :entity "mod")])

  (def world3
    {::ecs/kind :world
     ::ecs/systems
     {"my system" {::ecs/kind :system
                   ::ecs/system-id "my system"
                   :system :modification}}
     ::ecs/entities
     {1 {::ecs/kind :entity
         ::ecs/entity-id 1
         ::ecs/type ::one-kind
         ::ecs/components {}
         :entity "mod"}
      2 {::ecs/kind :entity
         ::ecs/entity-id 2
         ::ecs/type ::another-kind
         ::ecs/components
         {"comp1" {::ecs/kind :component
                   ::ecs/type ::component-a
                   ::ecs/system-id "my system"
                   ::ecs/entity-id 2
                   ::ecs/component-key "comp1"}}}}})


  (deftest return-multi
    (is (= (ecs/do-handle-event {::ecs/kind :to-entity
                                 ::ecs/entity-id 1}
                                {::ecs/msg ::test-event-return-multi}
                                0
                                world2)
         world3))))

