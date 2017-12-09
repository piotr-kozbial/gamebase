(ns gamebase.event-queue)

(def EVENT-BUFFER-SIZE 100)

(declare my-swap!)

(defn event-queue [root-atom ks]
  (let [q {:root-atom root-atom :ks ks}]
    (my-swap! q (fn [_] {:set_ #{}
                      :sq 0
                        :n 0}))
    q))

(defn my-swap! [{:keys [root-atom ks]} f]
  (swap! root-atom
         update-in ks f))

(defn my-deref [{:keys [root-atom ks]}]
  (get-in @root-atom ks))


(defn put-event! [qa event]
  (my-swap!
   qa
   (fn [{:keys [set_ sq n] :as q}]
     (assoc q
            :set_ (conj set_ (assoc event :sq sq))
            :sq (inc sq)
            :n (inc n)))))

(defn take-event! [qa]
  (my-swap!
   qa
   (fn [{:keys [set_ n] :as q}]
     (assoc q
            :set_ (rest set_)
            :n (dec n)
            :to-take (first set_))))
  (:to-take (my-deref qa)))


(defn soonest-time [qa]
  (when-let [fst (first (:set_ (my-deref qa)))]
    (:time fst)))

(defn seq-ready-events [qa time]
  (let [all (:set_ (my-deref qa))]
    (my-swap! qa (fn [{:keys [set_] :as q}]
                (assoc q :set_
                       (remove #(<= (:time %) time) set_))))
    (->> all
         (filter #(<= (:time %) time))
         (sort-by :time))))

(defn pop-soonest-event-until [qa time]
  (let [all (:set_ (my-deref qa))
        soonest (->> all
                     (filter #(<= (:time %) time))
                     (sort-by :time)
                     (first))]
    (when soonest
      (my-swap! qa (fn [{:keys [set_ n] :as q}]
                  (assoc q
                         :set_ (disj set_ soonest)
                         "n" (dec n))))
      soonest)))

(defn clear-queue [qa]
  (my-swap! qa (fn [q] (assoc q :set_ #{} :sq 0 :n 0))))

