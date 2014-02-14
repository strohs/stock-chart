(ns chart.core
  (:require [clojure.string :as str]
            [chart.utils :as utils])
  (:use [chart.datasources.yahoo]
        [chart.datasources.briefing]
        [chart.earnings]
        [incanter core stats charts]))

;path to directory that contains earnings data html files
;(def ^:dynamic *earnings-path* "C:\\Users\\Cliff\\IdeaProjects\\stock-chart\\earnings")
(def ^:dynamic *earnings-path* "C:\\Users\\Cliff\\IdeaProjects\\stock-chart\\dev-resources\\public\\earnings")



(defn prices-by-dates
  "given a list of joda dates and ClosingData, return closing prices for those dates
   as a seq of maps {:date date :price key}"
  [prices dates key]
  (let [date-set (into #{} dates)]
    (reduce (fn [acc {value key date :date}]
              (if (date-set date)
                (conj acc {:date date key value})
                acc)) [] prices)))



(defn chart
  ([ticker release-date]
   (chart ticker release-date (str *earnings-path* java.io.File/separator ticker ".html")))
  ([ticker release-date earnings-path]
   (let [date (utils/str->joda release-date "yyyy-MM-dd")
         earnings-data (get-earnings earnings-path)
         _ (println "Earnings: " earnings-data)
         price-data (get-prices ticker)
         _ (println "Prices: " (first price-data) (last price-data))
         data (dataset [:date :joda-date :close :adj-close :fiscal-quarter :year :pos :earnings-release]
                       (prices-by-earnings-date date earnings-data price-data))]
     (view (line-chart :pos :close :group-by :earnings-release :legend true :data data))
     )))

(defn -main [& args]
  (println "in -main function"))


;(-main "CRM" "2014-02-27" "src/crm.html")
;(view (time-series-plot :date :close :group-by :pos :legend true :data tdata))
;(def ed (get-earnings (str *earnings-path* java.io.File/separator "cgnx" ".html")))
;(def pd (get-prices "cgnx"))
