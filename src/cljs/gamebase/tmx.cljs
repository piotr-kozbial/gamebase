;;;;; STATUS: COMPLETE ;;;;;

(ns gamebase.tmx
  (:require
   ;;[clojure.data.xml :as xml]
   [clojure.string :as s]))

(defn parse-int [s]
  (js/parseInt s)
)

;; (defmacro examples [& body]
;;   `(do)
;;   ;; (let [triples (partition 3 3 nil body)]
;;   ;;   (assert (every? #(= 3 (count %)) triples))
;;   ;;   (assert (every? #(= '=> (second %)) triples))
;;   ;;   (let [assertions (->> triples
;;   ;;                         (map (fn [[a _ b]] (list '= a b)))
;;   ;;                         (map #(list 'assert %)))]
;;   ;;     `(do ~@assertions)))

;;   )

;;; This is to parse a tmx tile-map file format.

;;; We'll not support all of tmx possibilities. The restrictions will be mentioned
;;; along with the implementation.

;;; Let's see an example of such a file:

(def example-doc-text
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>

   <map version=\"1.0\" orientation=\"orthogonal\" renderorder=\"right-down\"
        width=\"5\" height=\"3\" tilewidth=\"32\" tileheight=\"32\" nextobjectid=\"1\">

     <tileset firstgid=\"1\" name=\"kafelki\" tilewidth=\"32\" tileheight=\"32\"
              tilecount=\"400\" columns=\"20\">
       <image source=\"tiles.png\" width=\"640\" height=\"640\"/>
     </tileset>

     <tileset firstgid=\"401\" name=\"background\" tilewidth=\"32\" tileheight=\"32\"
              tilecount=\"40\" columns=\"10\">
       <image source=\"background.png\" width=\"320\" height=\"128\"/>
     </tileset>

     <layer name=\"background\" width=\"5\" height=\"3\">
       <data encoding=\"csv\">
         401,401,401,401,401,
         401,401,401,401,401,
         401,401,401,401,401
       </data>
     </layer>

     <layer name=\"foreground\" width=\"5\" height=\"3\">
       <data encoding=\"csv\">
         1,4,4,62,63,
         24,0,0,0,0,
         24,0,0,0,0
       </data>
     </layer>

     <layer name=\"above\" width=\"5\" height=\"3\">
       <data encoding=\"csv\">
         0,0,0,0,0,
         0,0,0,0,0,
         0,0,0,0,0
       </data>
     </layer>

   </map>")

;;; Parse it as XML:

;; (def example-doc (xml/parse-str example-doc-text))

;;; And now we can start implementing our tmx-specific parser, which will take
;;; the parsed xml structure and return the same data in a nice usable form.

;;; First, we define a function to verify if the file is really a tmx file,
;;; and we also place constraints on what we can parse:

(defn verify-header [doc]
;;; Check if it looks like a tmx tiled map:
  (assert (= (:tag doc) :map))
;;; Now constraints:
  (let [attrs (:attrs doc)]
;;; - we only parse orthogonal maps:
    (assert (= (:orientation attrs) "orthogonal")
            "Can only handle tile maps in orthogonal mode (expected: orientation=\"orthogonal\")")
;;; - we only accept right-down tile ordering:
    (assert (= (:renderorder attrs) "right-down")
            "Can only handle tile maps in right-down mode (expected: renderorder=\"right-down\")")))

;;; Our example looks good in these aspects:

;; (examples
;;  (verify-header example-doc) => nil)

;;; Let's now read what's left of the header (or rather root node): the dimensions:

(defn parse-dimensions [doc]
  (let [{:keys [width height tilewidth tileheight]} (:attrs doc)]
    {;; width of map, in tiles
     :width (parse-int width)
     ;; height of map, in tiles
     :height (parse-int height)
     ;; width of tile, in pixels
     :tile-width (parse-int tilewidth)
     ;; height of tile, in pixels
     :tile-height (parse-int tileheight)}))

;;; In our case:

;; (examples
;;  (parse-dimensions example-doc) => {:width 5, :height 3
;;                                     :tile-width 32, :tile-height 32})

;;; We now turn to parsing the tileset information. First, extract all tilesets
;;; as xml subtrees:

(defn get-tileset-subdocs [doc]
  ;; tilesets are defined in <tileset> tags
  (filter #(= (:tag %) :tileset) (:content doc)))

;;; It works like this:

;; (examples

;;  (get-tileset-subdocs example-doc)

;;  =>

;;  (list

;;   #xml/element{:tag :tileset
;;                :attrs {:firstgid "1", :name "kafelki", :tilewidth "32", :tileheight "32"
;;                        :tilecount "400", :columns "20"}
;;                :content ["\n       "
;;                          #xml/element{:tag :image
;;                                       :attrs {:source "tiles.png"
;;                                               :width "640", :height "640"}}
;;                          "\n     "]}

;;   #xml/element{:tag :tileset
;;                :attrs {:firstgid "401", :name "background", :tilewidth "32", :tileheight "32"
;;                        :tilecount "40", :columns "10"}
;;                :content ["\n       "
;;                          #xml/element{:tag :image
;;                                       :attrs {:source "background.png"
;;                                               :width "320", :height "128"}}
;;                          "\n     "]}))

;;; And now the actual parsing of a single tileset element:

(defn parse-tileset [tileset-subdoc]
  (let [{:keys [firstgid name tilewidth tileheight tilecount columns]} (:attrs tileset-subdoc)
        image-subdoc (->> (:content tileset-subdoc)
                          (filter #(= (:tag %) :image))
                          (first))
        {:keys [source width height]} (:attrs image-subdoc)]
    {:name name
     :id-offset (parse-int firstgid)
     :tile-width (parse-int tilewidth)
     :tile-height (parse-int tileheight)
     :tile-count (parse-int tilecount)
     :tile-columns (parse-int columns)
     :image-file-name source
     :image-file-width (parse-int width)
     :image-file-height (parse-int height)}))

;;; and then to parse all tilesets in the whole file into a nice map,

(defn parse-tilesets [doc]
;;; we parse each one:
  (->> doc
       (get-tileset-subdocs)
       (map parse-tileset)
;;; then turn into a map where the neame of a tileset structure is the key, and
;;; the structure itself is the value:
       (mapcat (fn [{:keys [name] :as tileset}] [name tileset]))
       (apply hash-map)))

;;; Let's see the results:

;; (examples
;;  (parse-tilesets example-doc)
;;  =>
;;  {"background" {:name "background"
;;                 :id-offset 401
;;                 :tile-width 32
;;                 :tile-height 32
;;                 :tile-count 40
;;                 :tile-columns 10
;;                 :image-file-name "background.png"
;;                 :image-file-width 320
;;                 :image-file-height 128}
;;   "kafelki" {:name "kafelki"
;;              :id-offset 1
;;              :tile-width 32
;;              :tile-height 32
;;              :tile-count 400
;;              :tile-columns 20
;;              :image-file-name "tiles.png"
;;              :image-file-width 640
;;              :image-file-height 640}})

;;; And finally we need to parse the layers. First, extract all of them:

(defn get-layer-subdocs [doc]
  (filter #(= (:tag %) :layer) (:content doc)))

;; Let's try:

;; (examples

;;  (get-layer-subdocs example-doc)

;;  =>

;;  '({:tag :layer
;;     :attrs {:name "background", :width "5", :height "3"}
;;     :content ("\n       "
;;               {:tag :data
;;                :attrs {:encoding "csv"}
;;                :content ("
;;          401,401,401,401,401,
;;          401,401,401,401,401,
;;          401,401,401,401,401\n       ")}
;;               "\n     ")}
;;    {:tag :layer
;;     :attrs {:name "foreground", :width "5", :height "3"}
;;     :content ("\n       "
;;               {:tag :data,
;;                :attrs {:encoding "csv"},
;;                :content
;;                ("
;;          1,4,4,62,63,
;;          24,0,0,0,0,
;;          24,0,0,0,0\n       ")}
;;               "\n     ")}
;;    {:tag :layer
;;     :attrs {:name "above", :width "5", :height "3"}
;;     :content ("\n       "
;;               {:tag :data
;;                :attrs {:encoding "csv"}
;;                :content ("
;;          0,0,0,0,0,
;;          0,0,0,0,0,
;;          0,0,0,0,0\n       ")}
;;               "\n     ")}))

;;; And now the actual parsing of a single layer element:

(defn parse-layer [layer-subdoc]
  (let [{:keys [name width height]} (:attrs layer-subdoc)
        layer-width (parse-int width)
        layer-height (parse-int height)
        data (first (filter #(= (:tag %) :data) (:content layer-subdoc)))]
    (assert (= (:encoding (:attrs data)) "csv"))
    (let [id-list (first (:content data))]
      {:name name
       :width layer-width
       :height layer-height
       :id-list (->> id-list
                     (remove #{\newline \ \t})
                     (apply str)
                     (#(s/split % #","))
                     (map parse-int))})))

;;; and then to parse all layers in the whole file into a map, as we did for tilesets:

(defn parse-layers [doc]
  (->> doc
       (get-layer-subdocs)
       (map parse-layer)
       (mapcat (fn [{:keys [name] :as tileset}] [name tileset]))
       (apply hash-map)))

;;; In our case:

;; (examples
;;  (parse-layers example-doc)
;;  =>
;;  '{"foreground"
;;    {:name "foreground",
;;     :width 5,
;;     :height 3,
;;     :id-list (1 4 4 62 63 24 0 0 0 0 24 0 0 0 0)},
;;    "above"
;;    {:name "above",
;;     :width 5,
;;     :height 3,
;;     :id-list (0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)},
;;    "background"
;;    {:name "background",
;;     :width 5,
;;     :height 3,
;;     :id-list
;;     (401 401 401 401 401 401 401 401 401 401 401 401 401 401 401)}})

;;; Putting it all together:

(defn parse-tmx [doc]
  (verify-header doc)
  (assoc
   (parse-dimensions doc)
   :tilesets (parse-tilesets doc)
   :layers (parse-layers doc)))

;; (examples

;;  (parse-tmx example-doc)

;;  =>

;;  '{:width 5
;;    :height 3
;;    :tile-width 32
;;    :tile-height 32
;;    :tilesets {"background" {:name "background"
;;                             :id-offset 401
;;                             :tile-width 32
;;                             :tile-height 32
;;                             :tile-count 40
;;                             :tile-columns 10
;;                             :image-file-name "background.png"
;;                             :image-file-width 320
;;                             :image-file-height 128}
;;               "kafelki"    {:name "kafelki"
;;                             :id-offset 1
;;                             :tile-width 32
;;                             :tile-height 32
;;                             :tile-count 400
;;                             :tile-columns 20
;;                             :image-file-name "tiles.png"
;;                             :image-file-width 640
;;                             :image-file-height 640}}
;;    :layers {"foreground" {:name "foreground"
;;                           :width 5
;;                           :height 3
;;                           :id-list (1 4 4 62 63 24 0 0 0 0 24 0 0 0 0)}
;;             "above"      {:name "above"
;;                           :width 5
;;                           :height 3
;;                           :id-list (0 0 0 0 0 0 0 0 0 0 0 0 0 0 0)}
;;             "background" {:name "background"
;;                           :width 5
;;                           :height 3
;;                           :id-list
;;                           (401 401 401 401 401 401 401 401 401 401 401 401 401 401 401)}}})
