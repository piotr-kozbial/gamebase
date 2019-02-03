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

            (or custom-html [:div {:id "app"}])
            [:script {:src "gamebase/p5.js" :type "text/javascript"}]
            [:script {:src "app.js" :type "text/javascript"}]

            ;;[:script {:type "text/javascript"} "app.system.go();"]
            [:script
             (str "function setup() { app.system.go(); " main "();\n }\n")

             "function draw()          {
 gamebase.events.callback(\"draw\"); }\n"


        "function keyTyped()      { return gamebase.events.callback(\"canvas-key-typed\"); }\n"
        "function mousePressed()  { return gamebase.events.callback(\"canvas-mouse-pressed\"); }\n"
        "function mouseMoved()    { return gamebase.events.callback(\"canvas-mouse-moved\"); }\n"
        "function mouseDragged()  { gamebase.events.callback(\"mouse-dragged\"); gamebase.events.callback(\"canvas-mouse-dragged\");  return false; }\n"
        "function mouseReleased() { return gamebase.events.callback(\"canvas-mouse-released\"); }\n"
        "function mouseClicked()  { return gamebase.events.callback(\"canvas-mouse-clicked\"); }\n"
        "function mouseScrolled() { gamebase.events.callback(\"canvas-mouse-scrolled\"); }\n"
        "function windowResized() { gamebase.events.callback(\"window-resized\"); }\n"]]]))))

