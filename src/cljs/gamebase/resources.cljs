(ns gamebase.resources

  (:require
   [gamebase.tmx :as tmx]
   [clojure.string :as str]

   ;; [goog.dom :as gdom]
   )
  )



(declare all-resources-loaded?)

(def on-all-loaded (atom (fn [])))

(defn set-on-all-loaded [f]
  (reset! on-all-loaded f)
  (when (all-resources-loaded?)
    (f)))


(defonce resources (atom {}))
(def on-change (atom (fn [])))


(defn- file-type [fname]
  (cond
    (.endsWith fname ".png") :img
    (.endsWith fname ".tmx") :tmx
    :else nil))

(defmulti start-loading (fn [fname callback] (file-type fname)))
(defmethod start-loading :img [fname callback]
  (js/loadImage fname callback))
(defmethod start-loading :tmx [fname callback]
  (js/loadStrings fname callback))

(defmulti parse-and-store (fn [fname & args] (file-type fname)))
(defmethod parse-and-store :img [fname img]
  (swap! resources assoc fname img)
  (when (all-resources-loaded?)
    (@on-all-loaded)))
(defmethod parse-and-store :tmx [fname lines]
  (let [text (str/join "\n" lines)]
    (swap! resources assoc fname (tmx/read-tmx text)))
  (when (all-resources-loaded?)
    (@on-all-loaded)))

(defn add-resource [fname]
  (start-loading fname (fn [& args] (apply parse-and-store fname args))))
(defn remove-resource [fname]
  (swap! resources dissoc fname))
(defn all-resource-names []
  (keys @resources))
(defn resource-loaded? [fname]
  (not (nil? (@resources fname))))
(defn all-resources-loaded? []
  (every? resource-loaded? (all-resource-names)))
(defn get-resource [fname]
  (@resources fname))


