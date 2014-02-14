(ns chart.core
  (:require [clojure.string :as str]
            [chart.utils :as utils]
            [clojure.set :as set])
  (:use [chart.datasources.yahoo]
        [chart.datasources.briefing]
        [chart.earnings]
        [incanter core stats charts]))

;path to directory that contains earnings data html files
;(def ^:dynamic *earnings-path* "C:\\Users\\Cliff\\IdeaProjects\\stock-chart\\earnings")
(def ^:dynamic *earnings-path* "C:\\Users\\Cliff\\IdeaProjects\\stock-chart\\dev-resources\\public\\earnings")

(defn- with-pivot-date
  "add a position index to a collection of maps. Position index is used to line up data on when displaying charts
  so that the 0 position becomes the alignment point"
  [price-data joda]
  (let [pidx (utils/find-index (map :date price-data) joda)
        pos-range (range (- pidx) (- (count price-data) pidx))]
    (map #(assoc %1 :pos %2) price-data pos-range)))

(defn prices-by-dates
  "given a list of joda dates and ClosingData, return closing prices for those dates
   as a seq of maps {:date date :price key}"
  [price-data dates key]
  (let [date-set (into #{} dates)]
    (reduce (fn [acc {value key date :date}]
              (if (date-set date)
                (conj acc {:date date key value})
                acc)) [] price-data)))

(defn- prices-by-day-range
  "get price data within a range of days before and days after 'date'.
  Retuns a coll of price data, else nil if date not found"
  [prices date days-before days-after]
  ;get index of first matching date in prices and return a slice of prices else nil
  (if-let [date-idx (first (keep-indexed #(if (= (:date %2) date) %1) prices))]
    (do
      (utils/slice prices (- date-idx days-before) (+ date-idx days-after 1)))
    (list)))

(defn chart-day-range
  "get stock prices for a range of dates before and after the input date
  returns a sequence of closing data maps"
  [ticker date before after]
  (let [joda (utils/str->joda date)
        data (with-pivot-date (prices-by-day-range (get-prices ticker) joda before after) joda)]
    (map #(set/rename-keys % {:pos :x :close :y}) data)))


;(defn chart
;  ([ticker release-date]
;   (chart ticker release-date (str *earnings-path* java.io.File/separator ticker ".html")))
;  ([ticker release-date earnings-path]
;   (let [date (utils/str->joda release-date "yyyy-MM-dd")
;         earnings-data (get-earnings earnings-path)
;         _ (println "Earnings: " earnings-data)
;         price-data (get-prices ticker)
;         _ (println "Prices: " (first price-data) (last price-data))
;         data (dataset [:date :joda-date :close :adj-close :fiscal-quarter :year :pos :earnings-release]
;                       (prices-by-earnings-date date earnings-data price-data))]
;     (view (line-chart :pos :close :group-by :earnings-release :legend true :data data))
;     )))

(defn -main [& args]
  (println "in -main function"))


;(-main "CRM" "2014-02-27" "src/crm.html")
;(view (time-series-plot :date :close :group-by :pos :legend true :data tdata))
;(def ed (get-earnings (str *earnings-path* java.io.File/separator "cgnx" ".html")))
;(def pd (get-prices "cgnx"))
