(ns gamebase.events)

(declare canvasOnMousePressed canvasOnMouseMoved canvasOnMouseDragged
         canvasOnMouseReleased canvasOnMouseClicked canvasOnMouseScrolled
         onWindowResized)

;; API

(defn setCanvasOnMousePressed [handler]  (reset! canvasOnMousePressed handler))
(defn setCanvasOnMouseMoved [handler]    (reset! canvasOnMouseMoved handler))
(defn setCanvasOnMouseDragged [handler]  (reset! canvasOnMouseDragged handler))
(defn setCanvasOnMouseReleased [handler] (reset! canvasOnMouseReleased handler))
(defn setCanvasOnMouseClicked [handler]  (reset! canvasOnMouseClicked handler))
(defn setCanvasOnMouseScrolled [handler] (reset! canvasOnMouseScrolled handler))

(defn setOnWindowResized [handler] (reset! onWindowResized handler))

;; PRIVATE

(def canvasOnMousePressed (atom (fn [_])))
(def canvasOnMouseMoved (atom (fn [_])))
(def canvasOnMouseDragged (atom (fn [_])))
(def canvasOnMouseReleased (atom (fn [_])))
(def canvasOnMouseClicked (atom (fn [_])))
(def canvasOnMouseScrolled (atom (fn [_])))

(def onWindowResized (atom (fn [_])))

;; TODO: I tak samo keyPressed, keyReleased, keyTyped, - eventy, a takze funkcja keyIsDown(key)

;; TODO: Trzeba chyba zrobic jednak cale listy handlerow dla kazdego eventu. Zeby np. user mogl uzywac onWindowResized,
;;       chociaz nasze layouty musza. Tu by tylko trzeba zapewnic, ze user bedzie na koncu... hmmm...

;; TODO: Jednak glupio to wszystko kopiowac, zrobmy generyczne funkcje:
;;  (addHandler [event handler])  - i tutaj event to bedzie np. :mouse-pressed, :window-resized itd.
;; (addHandler, nie setHandler, bo chcemy miec listy)

(defn- mouseInCanvas []
  (and (<= 0 js/mouseX) (< js/mouseX js/width)
       (<= 0 js/mouseY) (< js/mouseY js/height)))

(defn- pmouseInCanvas []
  (and (<= 0 js/pmouseX) (< js/pmouseX js/width)
       (<= 0 js/pmouseY) (< js/pmouseY js/height)))

(defn ^:export canvasMousePressed [])
(defn ^:export canvasMouseMoved [])
(defn ^:export canvasMouseDragged []
  (when (and (mouseInCanvas) (pmouseInCanvas))
    (@canvasOnMouseDragged {:button js/mouseButton
                            :x js/mouseX
                            :y js/mouseY
                            :prev-x js/pmouseX
                            :prev-y js/pmouseY})))
(defn ^:export canvasMouseReleased [])
(defn ^:export canvasMouseClicked []
  (when (mouseInCanvas)
    (@canvasOnMouseClicked {:button js/mouseButton
                            :x js/mouseX
                            :y js/mouseY})))
(defn ^:export canvasMouseScrolled [])


(defn ^:export windowResized []
  (@onWindowResized {}))
