(ns gamebase.events)

(declare canvasOnMousePressed canvasOnMouseMoved canvasOnMouseDragged
         canvasOnMouseReleased canvasOnMouseClicked canvasOnMouseScrolled)

;; API

(defn setCanvasOnMousePressed [handler]  (reset! canvasOnMousePressed handler))
(defn setCanvasOnMouseMoved [handler]    (reset! canvasOnMouseMoved handler))
(defn setCanvasOnMouseDragged [handler]  (reset! canvasOnMouseDragged handler))
(defn setCanvasOnMouseReleased [handler] (reset! canvasOnMouseReleased handler))
(defn setCanvasOnMouseClicked [handler]  (reset! canvasOnMouseClicked handler))
(defn setCanvasOnMouseScrolled [handler] (reset! canvasOnMouseScrolled handler))

;; PRIVATE

(def canvasOnMousePressed (atom (fn [_])))
(def canvasOnMouseMoved (atom (fn [_])))
(def canvasOnMouseDragged (atom (fn [_])))
(def canvasOnMouseReleased (atom (fn [_])))
(def canvasOnMouseClicked (atom (fn [_])))
(def canvasOnMouseScrolled (atom (fn [_])))

UWAGA! JEdnak musze recznie sprawdzac, czy te eventy sa w canvasie.
Moze nie ma sensu w ten sposob - moze zwykle HTML-owe eventy na obiekcie canvas? Czy wtedy drag bedzie
tak samo dobrze opisany?

No dobra, ale to w miare latwo sprawdzic, bo sa zmienne w p5:

js/mouseX, js/mouseY - pozycja myszki WZGLEDEM CANVASU
js/pmouseX, js/pmouseY - poprzednia pozycja myszki WZGLEDEM CANVASU
js/width, js/height - wymiary CANVASU

Btw. jest tez do zdefiniowania windowResized(); - event w index.html - to bysmy nie musieli juz goog.events uzywac
I tak samo keyPressed, keyReleased, keyTyped, - eventy, a takze funkcja keyIsDown(key)

UZYC TEGO!!!



(defn ^:export canvasMousePressed [])
(defn ^:export canvasMouseMoved [])
(defn ^:export canvasMouseDragged []
  (@canvasOnMouseDragged {:button js/mouseButton
                    :x js/mouseX
                    :y js/mouseY
                    :prev-x js/pmouseX
                    :prev-y js/pmouseY}))
(defn ^:export canvasMouseReleased [])
(defn ^:export canvasMouseClicked []
  (@canvasOnMouseClicked {:button js/mouseButton
                          :x js/mouseX
                          :y js/mouseY}))
(defn ^:export canvasMouseScrolled [])
