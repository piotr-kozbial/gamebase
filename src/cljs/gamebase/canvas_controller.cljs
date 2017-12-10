(ns gamebase.canvas-controller
  (:require
   [gamebase.events :as events]))

;; (defn setup-drag-event []
;;   (events/add-handler :canvas-mouse-dragged
;;                       (fn [{:keys [button x y prev-x prev-y]}]
;;                         (when (= button js/RIGHT)
;;                           (let [{:keys [canvas-width canvas-height]} @layout
;;                                 dx (- x prev-x)
;;                                 dy (- y prev-y)
;;                                 scale (:scale @global/app-state)]
;;                             (swap! global/app-state
;;                                    (fn [{:keys [top-left-x top-left-y] :as st}]
;;                                      (let [[new-top-left-x new-top-left-y]
;;                                            ,   (correct-top-left (- top-left-x (/ dx scale))
;;                                                                  (- top-left-y (/ dy scale)))]
;;                                        (assoc st :top-left-x new-top-left-x
;;                                               :top-left-y new-top-left-y))))


;;                             )))))
