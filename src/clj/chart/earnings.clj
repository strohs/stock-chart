(ns chart.earnings
  (:require [clojure.string :as str]
            [clj-time.coerce :as ctc]
            [chart.utils :as utils]
            [clj-time.core :as jt])
  (:use [chart.datasources.yahoo]
        [chart.datasources.briefing]))

;package for manipulating earnings data and stock price data
;(defrecord EarningsPriceData)



(defn prices-by-earnings-date
  "group historical price data and earnings data into a single map"
  [release-date earnings prices]
  (let [equarters (same-quarter earnings release-date) ;get all earnings data the same quarter as release date
        ]
    (for [earnings-date equarters
          price-data (map #(assoc %1 :pos %2) (prices-by-day-range prices (:release-date-joda earnings-date) 20 31) (range -20 31))
          :let [datum (assoc {}
                        :date (ctc/to-epoch (get price-data :date))
                        :earnings-release (utils/joda->str (get earnings-date :release-date-joda) "yyyy-MMM-dd")
                        :joda-date (get price-data :date)
                        :close (get price-data :close)
                        :adj-close (get price-data :adj-close)
                        :fiscal-quarter (get earnings-date :fiscal-quarter)
                        :year (jt/year (get price-data :date))
                        :pos (get price-data :pos))]]
      datum)))
