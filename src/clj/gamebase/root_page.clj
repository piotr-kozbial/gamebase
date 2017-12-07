(ns gamebase.root-page
  (:require
   [hiccup.core :refer [html]]))

(defn mk-root-page [& options]

  (let [{:keys [app-js p5-js
                custom-html]}
        (apply hash-map options)]

    (str "\n\n\n\n\n"

         "<!DOCTYPE html>\n"

         (html
          [:html
           [:head
            [:meta {:charset "UTF-8"}]
            [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
            [:link {:href "css/style.css" :rel "stylesheet"
                    :type "text/css"}]]
           [:body {:id "body" :style "width:100%; height:100%"
                   :oncontextmenu "return false;"}

            (or custom-html [:div])

            [:script {:src "gamebase/p5.js" :type "text/javascript"}]
            [:script {:src app-js :type "text/javascript"}]

            [:script
             ;; TODO: te makstycoon. wywalic, zrobic nasz
             ;; biblioteke, w ktora sie to wepnie
             "function setup() { makstycoon.core.setup(); }\n"
             "function draw() { makstycoon.core.draw(); }\n"
             ;; TODO: a te moze w ogole niepotrzebne,
             ;; wepnie sie w konkretne elementy, a nie globalnie?
             "function mouseDragged() {makstycoon.globevents.mouseDragged(); return false;}\n"
             "function mousePressed() {makstycoon.globevents.mousePressed(); return false;}\n"]]]))))
