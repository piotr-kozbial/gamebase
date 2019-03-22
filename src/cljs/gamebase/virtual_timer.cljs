(ns gamebase.virtual-timer)

(defn mk-timer []
  {:running? false})

(defn run [timer virtual-time]
  {:running? true, :offset (- virtual-time (int (js/millis)))})

(defn stop [timer]
  {:running? false})

(defn running? [timer]
  (:running? timer))

(defn get-time [{:keys [running? offset]}]
  "Get current virtual time. Timer must be running."
  (assert running?)
  (+ (int (js/millis)) offset))

