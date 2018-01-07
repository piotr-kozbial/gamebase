(ns gamebase.events)

(def all-handlers (atom {}))

(defn add-handler [event-key handler]
  (swap! all-handlers update-in [event-key] #(conj (or % []) handler)))

(def canvas-events-on? (atom true))

(defn stop-canvas-events []
  (reset! canvas-events-on? false))

(defn resume-canvas-events []
  (reset! canvas-events-on? true))

(defmulti data-for-event (fn [event-key] event-key))
(defmulti precondition-for-event (fn [event-key] event-key))

(defn ^:export callback [event-name]
  (let [event-key (keyword event-name)
        handlers (event-key @all-handlers)]
    (when (precondition-for-event event-key)
      (doseq [h handlers]
        (h (data-for-event event-key))))))

;;-------------------------------------------------------------------

(defmethod data-for-event :draw [_]
  {})
(defmethod data-for-event :canvas-mouse-pressed [_]
  {:button js/mouseButton
   :x js/mouseX
   :y js/mouseY})
(defmethod data-for-event :canvas-mouse-moved [_]
  {:button js/mouseButton
   :x js/mouseX
   :y js/mouseY})
(defmethod data-for-event :canvas-mouse-dragged [_]
  {:button js/mouseButton
   :x js/mouseX
   :y js/mouseY
   :prev-x js/pmouseX
   :prev-y js/pmouseY})
(defmethod data-for-event :canvas-mouse-released [_]
  {:button js/mouseButton
   :x js/mouseX
   :y js/mouseY})
(defmethod data-for-event :canvas-mouse-clicked [_]
  {:button js/mouseButton
   :x js/mouseX
   :y js/mouseY})
(defmethod data-for-event :canvas-mouse-scrolled [_]
  {:button js/mouseButton
   :x js/mouseX
   :y js/mouseY})
(defmethod data-for-event :window-resized [_]
  {})

;;-------------------------------------------------------------------

(defn- mouseInCanvas []
  (and (<= 0 js/mouseX) (< js/mouseX js/width)
       (<= 0 js/mouseY) (< js/mouseY js/height)))
(defn- pmouseInCanvas []
  (and (<= 0 js/pmouseX) (< js/pmouseX js/width)
       (<= 0 js/pmouseY) (< js/pmouseY js/height)))

(defmethod precondition-for-event :draw [_] true)
(defmethod precondition-for-event :canvas-mouse-pressed [_] (and @canvas-events-on? (mouseInCanvas)))
(defmethod precondition-for-event :canvas-mouse-moved [_] (and @canvas-events-on? (mouseInCanvas)))
(defmethod precondition-for-event :canvas-mouse-dragged [_] (and @canvas-events-on? (mouseInCanvas) (pmouseInCanvas)))
(defmethod precondition-for-event :canvas-mouse-released [_] (and @canvas-events-on? (mouseInCanvas)))
(defmethod precondition-for-event :canvas-mouse-clicked [_] (and @canvas-events-on? (mouseInCanvas)))
(defmethod precondition-for-event :canvas-mouse-scrolled [_] (and @canvas-events-on? (mouseInCanvas)))
(defmethod precondition-for-event :window-resized [_] true)
