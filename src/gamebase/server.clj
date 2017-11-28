(ns gamebase.server
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [compojure.core :refer [GET POST routes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [response]]
            [ring.util.request :refer [body-string]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            ;; [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [com.stuartsierra.component :as component]
            [system.components.endpoint :refer [new-endpoint]]
            [system.components.handler :refer [new-handler]]
            [system.components.middleware :refer [new-middleware]]
            [system.components.jetty :refer [new-web-server]]))

(defn is->str [is]
  (let [rdr (clojure.java.io/reader is)]
    (slurp rdr)))

(defn home-routes [root-page message-handler]
  (fn [_]
    (routes
     (GET "/" _
          (-> root-page ;; "public/index.html"
              io/resource
              io/input-stream
              response
              (assoc :headers {"Content-Type" "text/html; charset=utf-8"})))

     (POST "/" req
           (try
             ;; (println (is->str (:body req)))
             ;; "ok"
             (->> req
                  (:body)
                  (is->str)
                  (read-string)
                  (message-handler)
                  (pr-str))

             ))

     (resources "/"))))

(defn app-system [config]
  (component/system-map
   :routes     (new-endpoint (home-routes (:root-page config)
                                          (:message-handler config)))
   :middleware (new-middleware {:middleware (:middleware config)})
   :handler    (-> (new-handler)
                   (component/using [:routes :middleware]))
   :http       (-> (new-web-server (:http-port config) nil {:host (:host config)})
                   (component/using [:handler]))))

(comment ;; example config
  (def conf {:host "127.0.0.1"
             :http-port 10555
             :root-page "public/index.html"
             :message-handler (fn [msg] {:dostalem msg})})

  (start-server conf)

  )

(defn start-server [config]
  (let [config'
        (merge config
               {:middleware [
                             [wrap-defaults api-defaults]
                             ;; wrap-body-string
                             wrap-with-logger
                             ;; wrap-gzip
                             ]})])
  (-> config
      app-system
      component/start)
  (println "Started server on "
           (str "http://" (:host config) ":" (:http-port config))))
