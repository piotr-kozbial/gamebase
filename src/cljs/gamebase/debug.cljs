(ns gamebase.debug)

;;; This namespace holds debugging settings,
;;; which can be switched at runtime.


(defonce settings
  (atom
   {
    :canvas-control {:coordinate-system-marker true}
    :debug-overlay-on false

    }))

(comment

  ;; switch coord system on
  (swap! gamebase.debug/settings assoc-in [:canvas-control :coordinate-system-marker] true)

  ;; switch coord system off
  (swap! gamebase.debug/settings assoc-in [:canvas-control :coordinate-system-marker] false)



  )
