(ns chart.core
  (:import (java.util NoSuchElementException))
  (:require [clojure.string :as str]
            [clj-time.coerce :as jtc]
            [clj-time.core :as jt]
            [clojure.set :as set])
  (:use [chart.datasources.yahoo]
        [chart.utils]
        [chart.datasources.briefing]))
        ;[incanter core stats charts]

;;This namespace is the main entry point for retrieving daily quotes and earnings release dates



(defn- with-pivot-date
  "add a position index to a collection of maps. Position index is used to line up data when displaying
  rickshaw charts so that the 0 position becomes the alignment point"
  [price-data joda]
  (if-let [pidx (find-index (map :date price-data) joda)]
    (let [pos-range (range (- pidx) (- (count price-data) pidx))]
      (map #(assoc %1 :pos %2) price-data pos-range))
    (throw (IllegalArgumentException. (str "date: " joda " not found in price data")))))

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
  Returns a coll of price data, else nil if date not found"
  [price-data date days-before days-after]
  ;get index of first matching date in prices and return a slice of prices else nil
  (if-let [date-idx (first (keep-indexed #(if (= (:date %2) date) %1) price-data))]
    (do
      (slice price-data (- date-idx days-before) (+ date-idx days-after 1)))
    (list)))

;TODO see if this method is still needed
;(defn prices-by-earnings-date
;  "group historical price data and earnings data into a single map"
;  [date earnings prices]
;  (let [ release-date (str->joda date)
;         equarters (same-quarter earnings date) ;get all earnings data the same quarter as release date
;        ]
;    (for [earnings-date equarters
;          price-data (with-pivot-date (prices-by-day-range prices (:release-date-joda earnings-date) 20 31) (:release-date-joda earnings-date))
;          :let [datum (assoc {}
;                        :date (jtc/to-epoch (get price-data :date))
;                        :earnings-release (joda->str (get earnings-date :release-date-joda) "yyyy-MMM-dd")
;                        :joda-date (get price-data :date)
;                        :close (get price-data :close)
;                        :adj-close (get price-data :adj-close)
;                        :fiscal-quarter (get earnings-date :fiscal-quarter)
;                        :year (jt/year (get price-data :date))
;                        :pos (get price-data :pos))]]
;      datum)))

;;Functions starting with chart- are for use in javscripting charting
(defn chart-day-range
  "get stock prices for a range of dates before and after the input date
  returns a sequence of closing data maps"
  [ticker date days-before days-after]
  (let [price-data (get-prices ticker)]    ;get daily closing prices from out Finance URL
    (when (contains-date? price-data date) ;make sure price data contains the release date
      (let [joda (str->joda date)
            data (with-pivot-date (prices-by-day-range price-data joda days-before days-after) joda)
            cleaned (map #(update-in % [:date] joda->str "yyyy-MM-dd") data)]
        (map #(set/rename-keys % {:pos :x :close :y}) cleaned)))))


(defn chart-earnings-range
  "get closing price data around all historical earnings release dates.
  returns a coll containing colls of maps, each sub coll contains the price data map for the earnings quarter"
  [ticker release-date]
  (let [quarterly-earnings (same-quarter (parse-earnings ticker) release-date)
        _ (println (log-data quarterly-earnings))
        final-data (for [qe quarterly-earnings ;all earnings for release-date quarter
                         :let [er-date (:release-date qe)  ;earnings release-date
                               year (jt/year (:release-date-joda qe))
                               qtr (:fiscal-quarter qe)
                               prices (chart-day-range ticker er-date 20 31);map prices for the release-date
                               her-data (map #(assoc % :year-qtr (str year "Q" qtr)) prices)]] ;append earnings info to the returned collection
                     her-data)]
       ;;filter out empty collections, these are quarters for which we have no price data
    (filter not-empty final-data)))




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
;;(def ed (get-earnings (str *earnings-path* "/cgnx" ".html")))
;;(def pd (get-prices "cgnx"))
