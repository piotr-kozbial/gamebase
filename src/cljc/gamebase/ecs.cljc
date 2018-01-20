(ns gamebase.ecs)

;; Entity-Component System common definitions


(defmulti component-handle-event
  (fn [sprite-id
      system-id
      component-id
      component                                       ;; komponent, ktory przerabiamy
       related-components ;; TODO: to beda inne komponenty z tego sprite'a,
       ;; read-only, wedlug recepty (wiring)
       total-time                         ;; read-only
       world                              ;; read-only
      event]
    [system-id (:type component)]))



;; TODO
(defn- handle-event-in-sprite [system-key sprite total-time world event]


  ;; TODO - handle multiple component per system

  (if-let [component ((::components sprite) system-key)]
    (let [component' (component-handle-event
                      (:id sprite)
                      system-key
                      nil
                      component
                      nil
                      total-time
                      world
                      event)
          sprite' (assoc-in sprite [::components system-key] component')]
      (assoc-in world [:sprites (:id sprite)] sprite'))
    world)


  )


(defn default-handle-event [{:keys [system-key handle-system-event]}
                            total-time world event]
  (let [target-id (:target-id event)]
    (case target-id
      ::system
      (handle-system-event total-time world event)

      ::broadcast
      (reduce
       (fn [w id]
         (handle-event-in-sprite system-key ((:sprites w) id) total-time w event))
       world
       (sort (keys (:sprites world))))

      ;; otherwise: specific sprite event
      (let [sprite ((:sprites world) target-id)]
        (handle-event-in-sprite system-key sprite total-time world event)))))



;; IDEAS

;; - moze jednak wygodniej od razu component-handle-event dispatchowac tez na (:msg event),
;;   bo zobacz jak to niemilo w makstycoon.systems.locomotive.loco, a wszedzie tak bedzie

;; - sprite-id, ::broadcast, ::system - to sa rozne "targety" eventow
;;   Moze jeszcze dodac component-id, czyli wlasciwie wektor [sprite-id system-id component-id],
;;   a jezeli w spricie jest tylko jeden komponent danego systemu, to mozna [sprite-id system-id]

;;   No i tez rozne funkcje, ktore tam biora sprite-id system-id component-id, niech moze biora "target", czy cos

;; - moze jednak spec na cos tu?

;; - jak juz mam duzo w cljc, to latwiej testowac
