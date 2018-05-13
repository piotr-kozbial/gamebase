(ns gamebase.draw
  (:require [gamebase.ecs :as ecs]))

;; Pytanie - czy wyglad sprite'a to nie powinien byc jakis system
;; (w sensie entity-component-system)? To nie tyle, ze samo rysowanie,
;; co np. animacje i rozne wyglady w zaleznosci od stanu itp.
;; Natomiast niniejsza funkcja bralaby "biezacy" img, rotacje itd. i to rysowala.
;; To wszystkie te w sumie atrybuty co bierzemy, z tymi zastrzezeniami:
;; - tutaj bierzemy (textures nil), a powinna byc jakas current-texture
;; - width/height - to dot. tekstury? jesli tak, to moze w samej teksturze powinno byc
(defn draw-sprite [{:keys [reset-transformation get-image]}
                   sprite0
                   ]

  (let [
        {:keys [tile-x tile-y textures current-texture
                local-x local-y
                angle
                width height
                center-x center-y]} (:locomotive (::ecs/components sprite0))

        ]
    (reset-transformation)

    (let [texture (get-image (textures current-texture))
          tile-left (* 32 tile-x)
          tile-bottom (* 32 (- 100 tile-y))]
      (js/translate
       (+ tile-left local-x)
       (+ tile-bottom (- local-y)))
      (js/angleMode js/DEGREES)
      (js/rotate (- angle))
      (js/image texture
                (- center-x) (- center-y) width height
                0 0 width height))))

;; Btw. zamiast img powinno byc nazwa (czy jakies id) resource'a z tile'ami,
;; a poza tym tiles-img i tile-offs powinny byc w strukturze layera.
;; (A wczytywanie ich po prostu ladowac sie powinno z tmx.
;; Oczywiscie jezeli sami konstruujemy layer, to co innego).
;;
;; UWAGA Z tym tile-offset to cos nie tak - zob. w pliku tmx. Tam sa tilesety,
;; ale nie przypisane do konkretnych layerow. Skad mi sie w ogole wzial pomysl na ten tile-offset?
;; A chyba wiem - zeby wiedziec numeracje kafelkow w obrazku kafelkow.
;; Ale to zle - powinno byc tile-offset i image przypisane do tilesetu.
;; Hmmm.... trzeba to wiec uporzadkowac.
(defn draw-tiled-layer [{:keys [reset-transformation get-image get-top-left]}
                        layer tiles-img tile-offs]

  (reset-transformation)
  (let [[top-left-x top-left-y visible-width visible-height] (get-top-left)

        h-tile-start (js/Math.floor (/ top-left-x 32))
        h-tile-count (inc (js/ceil
                           (/ visible-width 32)))
        h-tile-end (+ h-tile-start h-tile-count)
        v-tile-start (js/Math.floor (/ top-left-y 32))
        v-tile-count (inc (js/ceil
                           (/ visible-height 32)))
        v-tile-end (+ v-tile-start v-tile-count)
        img (get-image tiles-img)]
    (doall
     (map-indexed
      (fn [y row]
        (when (and (<= v-tile-start y) (< y v-tile-end))
          (doall
           (map-indexed
            (fn [x tile-number]
              (when (> tile-number 0)
                (let [tile-x (mod (- tile-number tile-offs) 20)
                      tile-y (quot (- tile-number tile-offs) 20)]
                  (js/image img,
                            (- (* x 32) 0) (- (* y 32) 0) 32 32,
                            (* tile-x 32) (* tile-y 32) 32 32))))
            row))))
      (:content layer)))))

;; TODO - dobrze przemysles strukture tiled layera i zrobic do niego spec.
