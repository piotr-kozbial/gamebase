(ns gamebase.tmx
  (:require
   [clojure.data.xml :as xml]
   [clojure.string :as s]))

(defn- read-layer [layer-xml-dom]
  (let [
        width (js/parseInt
               (:width (:attrs layer-xml-dom)))
        height (js/parseInt
                (:height (:attrs layer-xml-dom)))
        content (->> layer-xml-dom
                     :content (filter #(= (:tag %) :data)) (first)
                     :content (first)
                     (map identity)
                     (remove #{\newline})
                     (apply str)
                     (#(s/split % #","))
                     (map #(js/parseInt %)))]
    {:width width
     :height height
     :content (->> (partition width content)
                   (map #(apply vector %))
                   (apply vector))}))

(defn read-tmx-layer [tmx-file-text layer-name]
  (let [layer (->> tmx-file-text
                   (xml/parse-str)
                   :content
                   (filter #(and (= (:tag %) :layer)
                                 (= (-> % :attrs :name)
                                    layer-name)))
                   (first))]
    (read-layer layer)))

(defn read-tmx [tmx-file-text]
  (->> tmx-file-text
       (xml/parse-str)
       :content
       (filter #(and (= (:tag %) :layer)))
       (mapcat (fn [layer]
                 [(keyword (-> layer :attrs :name))
                  (read-layer layer)]))
       (apply hash-map)))
