(ns gamebase.root-page
  (:require
   [hiccup.core :refer [html]]))

(defn mk-root-page [& options]

  (let [{:keys [app-js p5-js
                custom-html
                main]}
        (apply hash-map options)]

    (str "\n\n\n\n\n"

         "<!DOCTYPE html>\n"

         (html
          [:html
           [:head
            [:meta {:charset "UTF-8"}]
            [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
            [:link {:href "public/css/style.css" :rel "stylesheet"
                    :type "text/css"}]]
           [:body {:id "body" :style "width:100%; height:100%"
                   :oncontextmenu "return false;"}

            (or custom-html [:div])
            [:script {:src "gamebase/p5.js" :type "text/javascript"}]
            [:script {:src "app.js" :type "text/javascript"}]

            [:script
             (str "function setup() { " main "(); }\n")

             "function draw()          { gamebase.events.callback(\"draw\"); }\n"

             "function mousePressed()  { gamebase.events.callback(\"canvas-mouse-pressed\");  return false; }\n"
             "function mouseMoved()    { gamebase.events.callback(\"canvas-mouse-moved\");    return false; }\n"
             "function mouseDragged()  { gamebase.events.callback(\"canvas-mouse-dragged\");  return false; }\n"
             "function mouseReleased() { gamebase.events.callback(\"canvas-mouse-released\"); return false; }\n"
             "function mouseClicked()  { gamebase.events.callback(\"canvas-mouse-clicked\");  return false; }\n"
             "function mouseScrolled() { gamebase.events.callback(\"canvas-mouse-scrolled\"); return false; }\n"
             "function windowResized() { gamebase.events.callback(\"window-resized\"); return false; }\n"
             ]]]))))

