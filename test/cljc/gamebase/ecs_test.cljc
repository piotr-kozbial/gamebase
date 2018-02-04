(ns gamebase.ecs-test
  (:require [clojure.test :refer :all]
            [schema.core :as s :include-macros true]
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

(do ;; event handling

  (deftest aaa
    (ecs/call-handle-event {::ecs/kind :to-world} :e :tt :w :o))

  )
