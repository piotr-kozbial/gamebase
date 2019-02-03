(ns gamebase.helpers)


(defmacro examples [& body]
  (let [triples (partition 3 3 nil body)]
    (assert (every? #(= 3 (count %)) triples))
    (assert (every? #(= '=> (second %)) triples))
    (let [assertions (->> triples
                          (map (fn [[a _ b]] (list '= a b)))
                          (map #(list 'assert %)))]
      `(do ~@assertions))))
