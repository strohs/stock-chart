(ns chart.core
  (:require [ajax.core :refer [GET POST]]
            [domina :as d]
            [domina.events :as events]))

;defing constants for our html ID fields
(def chart-container-id "chart_container")
(def chart-id "chart")
(def date-id "date")
(def ticker-id "ticker")
(def button-id "submit")

(defn log [s] (.log js/console (pr-str s)))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))




(defn days-format
  "custom formatter used for an x-axis"
  [n]
  (let [ranges (range -100 100)
        mapped (into {} (map #(vector % (str "days " %)) ranges))]
    (clj->js mapped)))

(defn build-chart [response]
  (log response))

(defn stock-prices [ticker date]
  (GET (str "/earnings/" ticker "/" date) {:response-format :json
                                           :keywords? false
                                           :handler build-chart
                                           :error-handler error-handler}))

(defn get-date []
  (.-value (d/by-id date-id)))

(defn get-ticker []
  (.-value (d/by-id ticker-id)))

(defn ^:export main []
  (events/listen! (d/by-id button-id)
                  :click
                  (fn [event]
                    (stock-prices (get-ticker) (get-date))
                    (events/stop-propagation event)
                    (events/prevent-default event))))