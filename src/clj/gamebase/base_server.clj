(ns gamebase.base_server
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [compojure.core :refer [GET POST routes]]
            [compojure.route :refer [resources]]
            [ring.util.response :refer [response]]
            [ring.util.request :refer [body-string]]
            [ring.adapter.jetty :refer [run-jetty]]))

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
                   exception-handler dev?]
  (routes
   (GET "/app.js" _
        (if dev?
          (io/input-stream (io/file "dev-target/public/js/compiled/app.js"))
          (io/input-stream (io/resource "public/js/compiled/app.js"))))
   (GET "/" _
        (-> (if (string? root-page)
              (io/input-stream (io/resource root-page))
              (root-page))
            response
            (assoc :headers
                   {"Content-Type"
                    "text/html; charset=utf-8"}))
        ;; "jasio - was - here"
        )
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
   (resources "/" {:root ""})))


(defn start-server [config]
  (run-jetty

   (home-routes
    (or (:root-page config) "index.html")
    (or (:message-handler config)
        (fn [msg] {:received msg}))
    (or (:exception-handler config)
        (fn [req ex] {:error (str ex)}))
    (:dev? config))


   {:host (:host config)
    :port (:http-port config)
    :join? false})

  (println "Started server on "
           (str "http://" (:host config)
                ":" (:http-port config))))

(defn build-handler [config]
  (home-routes
   (or (:root-page config) "index.html")
   (or (:message-handler config)
       (fn [msg] {:received msg}))
   (or (:exception-handler config)
       (fn [req ex] {:error (str ex)}))
   (:dev? config)))
