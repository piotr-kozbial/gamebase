(defproject gamebase "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring "1.6.2"]
                 [ring/ring-defaults "0.3.1"]
                 [radicalzephyr/ring.middleware.logger "0.6.0"]
                 [compojure "1.6.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [org.danielsz/system "0.4.0"]]
  :main ^:skip-aot gamebase.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
