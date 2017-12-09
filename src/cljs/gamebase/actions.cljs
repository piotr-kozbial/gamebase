(ns gamebase.actions)

(defmulti action (fn [system-key & args] system-key))

(defn post-action [system-key & args]
  (apply action system-key args))
