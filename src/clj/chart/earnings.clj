(ns chart.earnings
  (:require [clojure.string :as str]
            [clj-time.coerce :as ctc]
            [chart.utils :as utils]
            [clj-time.core :as jt])
  (:use [chart.datasources.yahoo]
        [chart.datasources.briefing]))

;package for manipulating earnings data and stock price data
;(defrecord EarningsPriceData)




