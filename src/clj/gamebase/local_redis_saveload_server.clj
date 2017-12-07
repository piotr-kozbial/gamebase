(ns gamebase.local-redis-saveload-server
  (:require [clojure.string :as str]
            [gamebase.base_server :as bs]
            [taoensso.carmine :as car :refer [wcar]]))


(comment ;; example usage

  (def conf {:host "127.0.0.1"
             :http-port 10555
             :root-page "index.html"})

  (start-server conf)

  )


(declare message-handler)


(defn start-server [config]

  (bs/start-server
   (assoc config
          :message-handler message-handler
          :exception-handler (fn [req ex] {:blad (str ex)}))))

(defn build-handler [config]
  (bs/build-handler
   (assoc config
          :message-handler message-handler
          :exception-handler (fn [req ex] {:blad (str ex)}))))

(defn get-game-name [redis-conn id]
  (car/wcar redis-conn (car/get (str "game:" id ":name"))))

(defn get-game-state [redis-conn id]
  (read-string (car/wcar redis-conn (car/get (str "game:" id ":state")))))

(defn get-games [redis-conn]
  (println "GET GAMES!!!")
  (->> (car/wcar redis-conn (car/keys "game:*:name"))
       (map #(let [id (Integer. (nth (str/split % #":") 1))] [id (get-game-name redis-conn id)]))))

(defn get-game [redis-conn id]
  (let [state (car/wcar redis-conn (car/get (str "game:" id ":state")))
        name (get-game-name redis-conn id)]
    {:id id
     :name name
     :state (read-string state)}))

(defn get-fresh-game-id [redis-conn]
  (->> (car/wcar redis-conn (car/keys "game:*:name"))
       (map #(Integer. (nth (str/split % #":") 1)))
       (sort)
       (last)
       (#(or % 0))
       (inc)))

(defn save-game [redis-conn id? name? state?]
  (println "SAVE!!!")
  ;; (= id nil) means "new game, assign id"
  ;; (not= id nil) means "update game with given id"
  ;; update name if not nil, update state if not nil
  (let [id (or id? (get-fresh-game-id redis-conn))]
    (when name?
      (car/wcar redis-conn (car/set (str "game:" id ":name") name?)))
    (when state?
      (car/wcar redis-conn (car/set (str "game:" id ":state") state?)))
    id))


(defn message-handler [{:keys [request] :as msg}]
  (let [redis-conn {:pool {} :spec {}}]
    (case request
      :load-game (get-game redis-conn (:id msg))
      :list-games (get-games redis-conn)
      :save-game (let [{:keys [state name id]} msg]
                   (let [id' (save-game redis-conn id name state)]
                     {:id id'})))))
