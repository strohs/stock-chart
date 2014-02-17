;;; If this namespace requires macros, remember that ClojureScript's
;;; macros are written in Clojure and have to be referenced via the
;;; :require-macros directive where the :as keyword is required, while in Clojure is optional. Even
;;; if you can add files containing macros and compile-time only
;;; functions in the :source-paths setting of the :builds, it is
;;; strongly suggested to add them to the leiningen :source-paths.
(ns wonderful-lib.core)

(defn foo [greeting]
  (if greeting
    (str greeting "ClojureScript!")
    (str "Hello, ClojureScript!")))

(.write js/document (foo "Welcome to "))



(defn generate-data
  "return n pieces of random :x :y data as a sequence of #js objects"
  [n]
  ()
  (let [random (Rickshaw.Fixtures.RandomData. n)
        data (clj->js [ [] [] [] ])]
    (dotimes [i n]
      (.addData random data))
    data))


(defn create-graph [data]
  (Rickshaw.Graph. (clj->js {
                              :element (.getElementById js/document "chart")
                              :width 960
                              :height 500
                              :renderer "line"
                              :series [ {:color "#c05020"
                                         :data  (get data 0)
                                         :name  "New York"
                                        }
                                        {:color "#30c020"
                                         :data  (get data 1)
                                         :name  "London"
                                        }
                                        {:color "#6060c0"
                                         :data  (get data 2)
                                         :name  "Tokyo"}]})))



(defn hover-detail [g] (Rickshaw.Graph.HoverDetail. (clj->js {:graph g})))

(defn create-legend [g] (Rickshaw.Graph.Legend. (clj->js {:graph g
                                                          :element (.getElementById js/document "legend")})))

(defn create-shelving [g l]
  (Rickshaw.Graph.Behavior.Series.Toggle. (clj->js {:graph g
                                                    :legend l})))

(defn create-axes [g] (Rickshaw.Graph.Axis.Time. (clj->js {:graph g})))

(defn create-y-axis [g]
  (Rickshaw.Graph.Axis.Y. (clj->js { :graph g
                                     :orientation "left"
                                     :tickFormat (Rickshaw.Fixtures.Number.formatKMBT)
                                     :element (.getElementById js/document "y_axis")})))

(defn render-axes [a] (.render a))

(defn render-graph [data]
  (let [g (create-graph data)
        _ (hover-detail g)
        l (create-legend g)
        _ (create-shelving g l)
        a (create-axes g)
        _ (create-y-axis g)]
    (.render g)
    (.render a)
    g))


