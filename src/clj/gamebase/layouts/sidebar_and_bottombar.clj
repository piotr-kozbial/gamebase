(ns gamebase.layouts.sidebar-and-bottombar)

(defn mk-html [& [splash-image]]
  [:div
   [:div {:id "gamebase/canvas-holder"
          :style "position:absolute; background-color: #888888"}]
   [:div
    {:id "gamebase/bottom-bar"
     :style "position:absolute;background-color:#BBA415"}]
   [:div
    {:id "gamebase/side-bar"
     :style "position:absolute; background-color:#C6AF20"}]
   [:div
    {:id "gamebase/fullpage"
     :style (str "position:absolute; left:0; top:0; "
                 "background-color:#C6AF20; width:100%; height:100%")}]
   (when splash-image
   [:div
    {:id "gamebase/splash" :style "position:absolute; z-index:200; width:100%; height:100%"}
    [:img
     {:src "loading.png" :style "position:absolute; width:100%; height:100%"}]])])
