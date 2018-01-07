(defproject gamebase "0.1.1-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.908" :scope "provided"]
                 [ring "1.6.2"]
                 [org.clojure/data.xml "0.2.0-alpha2"]
                 [ring/ring-defaults "0.3.1"]
                 [radicalzephyr/ring.middleware.logger "0.6.0"]
                 [compojure "1.6.0"]
                 [org.danielsz/system "0.4.0"]
                 [hiccup "1.0.5"]
                 [com.taoensso/carmine "2.16.0"]]
  :target-path "target/%s"

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.1.6"]]

  :profiles {:uberjar {:aot :all}}

  :cljsbuild {:builds
              [{:id "app"
                :source-paths ["src/cljs" "src/cljc"]

                :figwheel true

                :compiler {:main makstycoon.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/js/compiled/makstycoon.js"
                           :output-dir "resources/js/compiled/out"
                           :source-map-timestamp true
                           :optimizations :none
                           }}

               ;; {:id "test"
               ;;  :source-paths ["src/cljs" "src/cljc" "test/cljc"]
               ;;  :compiler {:output-to "resources/js/compiled/testable.js"
               ;;             :main makstycoon.test-runner
               ;;             :optimizations :none}}

               {:id "min"
                :source-paths ["src/cljs" "src/cljc"]
                :jar true
                :compiler {:main makstycoon.core
                           :output-to "resources/js/compiled/makstycoon.js"
                           :output-dir "target"
                           :source-map-timestamp true
                           :optimizations :advanced
                           :pretty-print false}}]}


  )
