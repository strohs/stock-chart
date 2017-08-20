(ns chart.builder)

;;; clojurescript namespace used to build the line graph details required by rickshaw

(def chart-container-id "chart_container")
(def chart-id "chart")
(def legend-id "legend")
(def x-axis-id "x_axis")
(def y-axis-id "y_axis")

(defn days-format
  "custom formatter used for an x-axis"
  [n]
  (let [ranges (range -100 100)
        mapped (into {} (map #(vector % (str "D " %)) ranges))]
    (get mapped n)))

(defn build-series
  "build a data series that can then be used in a chart. returns a vector of maps.
  data is the closing price/earnings data, name-key is the key used to name each sub-series
  ex. [ {:color c1 :name namekeyval :data [{} {} {}]} {:color c1 :name namekeyval :data [{} {} {}]}]"
  [data name-key]
  (let [palette (Rickshaw.Color.Palette.)]
    (reduce (fn [acc datum]
              (conj acc {:color (.color palette) :name (get (first datum) name-key) :data datum})) [] data)))


(defn create-graph [data]
  (Rickshaw.Graph. (clj->js {
                              :element (.getElementById js/document chart-id)
                              :width 800
                              :height 600
                              :renderer "line"
                              :interpolation "linear"
                              :series (build-series data :year-qtr)})))



(defn hover-detail [g] (Rickshaw.Graph.HoverDetail. (clj->js {:graph g})))

(defn create-legend [g] (Rickshaw.Graph.Legend. (clj->js {:graph g
                                                          :element (.getElementById js/document "legend")})))

(defn create-shelving [g l]
  (Rickshaw.Graph.Behavior.Series.Toggle. (clj->js {:graph g
                                                    :legend l})))

;(defn create-axes [g] (Rickshaw.Graph.Axis.Time. (clj->js {:graph g})))

(defn create-x-axis [g]
  (Rickshaw.Graph.Axis.X. (clj->js { :graph g
                                     :orientation "bottom"
                                     :tickFormat days-format
                                     :pixelsPerTick: 25
                                     :element (.getElementById js/document "x_axis")})))

(defn create-y-axis [g]
  (Rickshaw.Graph.Axis.Y. (clj->js {:graph      g
                                    :tickFormat (Rickshaw.Fixtures.Number.formatBase1024KMGTP)})))
                                    ;:orientation "left"
                                    ;:element (.getElementById js/document "y_axis")


(defn render-axes [a] (.render a))

(defn render-chart [data]
  ;clear any charts that was previously set
  (set! (. (.getElementById js/document chart-id) -innerHTML) "")
  (set! (. (.getElementById js/document legend-id) -innerHTML) "")
  (set! (. (.getElementById js/document x-axis-id) -innerHTML) "")
  (set! (. (.getElementById js/document y-axis-id) -innerHTML) "")

  (let [g (create-graph data)
        _ (hover-detail g)
        l (create-legend g)
        _ (create-shelving g l)
        a (create-x-axis g)
        _ (create-y-axis g)]
    (.render g)
    ;(.render a)
    g))