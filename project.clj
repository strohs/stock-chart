(defproject stock-chart "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [enlive "1.1.5"]
                 [incanter "1.5.4"]
                 [clj-time "0.6.0"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [compojure "1.1.6"]
                 [ring-json-response "0.2.0"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [cljs-ajax "0.2.3"]
                 [domina "1.0.2"]]

  ;; We need to add src/cljs too, because cljsbuild does not add its
  ;; source-paths to the project source-paths
  :source-paths ["src/clj" "src/cljs"]

  :injections [(use '[clojure.pprint :only [pprint]])]

  :plugins [[lein-ring "0.8.10"]
            [lein-cljsbuild "1.0.2"]]

  :hooks [leiningen.cljsbuild]
  :ring {:handler chart.web-site.routes/app}

  :cljsbuild
  {:builds {;; This build is only used for including any cljs source
            ;; in the packaged jar when you issue lein jar command and
            ;; any other command that depends on it
             :stock-chart-lib
             {:source-paths ["src/cljs"]
              ;; The :jar true option is not needed to include the CLJS
              ;; sources in the packaged jar. This is because we added
              ;; the CLJS source codebase to the Leiningen
              ;; :source-paths
              ;:jar true
              ;; Compilation Options
              :compiler
                            {:output-to "dev-resources/public/js/stock_chart_lib.js"
                             :optimizations :whitespace
                             :pretty-print false}}}})
