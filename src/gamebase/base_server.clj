(ns gamebase.base_server
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [compojure.core :refer [GET POST routes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [response]]
            [ring.util.request :refer [body-string]]
            [com.stuartsierra.component :as component]
            [system.components.endpoint :refer [new-endpoint]]
            [system.components.handler :refer [new-handler]]
            [system.components.jetty :refer [new-web-server]]))

(comment ;; example usage

  (defn message-handler [msg]
    {:dostalem msg})

  (def conf {:host "127.0.0.1"
             :http-port 10555
             :root-page "index.html"
             :message-handler #'message-handler
             :exception-handler (fn [req ex] {:blad (str ex)})})

  (start-server conf)

  ;; test with:
  ;;
  ;; $ curl 127.0.0.1:10555
  ;; $ curl -d '{:a 1, :b 2, :text "Jasio was here!"}' 127.0.0.1:10555

  )

(defn home-routes [root-page message-handler
                   exception-handler]
  (fn [_]
    (routes
     (GET "/" _
          (-> root-page
              io/resource
              io/input-stream
              response
              (assoc :headers
                     {"Content-Type"
                      "text/html; charset=utf-8"})))
     (POST "/" req
           (try
             (->> req
                  (body-string)
                  (read-string)
                  (message-handler)
                  (pr-str))
             (catch Throwable ex
               (println ex)
               (pr-str (exception-handler req ex)))))
     (resources "/"))))

(defn app-system [config]
  (component/system-map
   :routes     (new-endpoint
                (home-routes
                 (or (:root-page config) "index.html")
                 (or (:message-handler config)
                     (fn [msg] {:received msg}))
                 (or (:exception-handler config)
                     (fn [req ex] {:error (str ex)}))))
   :middleware (new-middleware {:middleware []})
   :handler    (-> (new-handler)
                   (component/using [:routes :middleware]))
   :http       (-> (new-web-server
                    (or (:http-port config) 10555)
                    nil
                    {:host (or (:host config) "127.0.0.1")})
                   (component/using [:handler]))))

(defn start-server [config]
  (-> config
      app-system
      component/start)
  (println "Started server on "
           (str "http://" (:host config)
                ":" (:http-port config))))

