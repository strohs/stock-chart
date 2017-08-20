(ns chart.core
  (:require [ajax.core :refer [GET POST]]
            [domina :as d]
            [domina.events :as events])
  (:use [chart.builder :only [render-chart]]))

;;;; Clojurescript main entry point for kicking off retrieval of earnings data and stock prices, These functions
;;;;  are included into the index.html page that displays are graphs

;def literal constants for our html ID fields
(def date-id "date")
(def ticker-id "ticker")
(def button-id "submit")
(def earnings-url "/earnings")

(defn get-date []
  (.-value (d/by-id date-id)))

(defn get-ticker []
  (.-value (d/by-id ticker-id)))

(defn log [s] (.log js/console (pr-str s)))

;(defn handler [response]
;  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))


(defn build-chart
  "[response] is a a json object containing data points used by RickShaw.js grpahing functions"
  [response]
  (log response)
  (render-chart response))


(defn stock-prices
  "call our local jetty server to retrieve stock prices for the ticker symbol and earnings release date
  returns - a clojure map, that gets converted into a JSON object via the AJAX/core library"
  [ticker date]
  (GET (str earnings-url "/" ticker "/" date) {:response-format :json
                                               :keywords? true
                                               :handler build-chart
                                               :error-handler error-handler}))

(defn ^:export main []
  ;;listen for a submit event from the form
  (events/listen! (d/by-id button-id)
                  :click
                  (fn [event]
                    (stock-prices (get-ticker) (get-date))
                    (events/stop-propagation event)
                    (events/prevent-default event)
                    (d/set-text! (d/by-id "title") (str "Quarterly Stock Price Movement for: " (get-ticker))))))