(ns chart.datasources.yahoo
  (:require [clojure-csv.core :as csv]
            [chart.utils :as utils]
            [clj-time.core :as jt]))

;;; namespace used to retrieve stock price data from finance.yahoo.com

;record for daily stock price closing data, date field should be a Joda DateTime object
(defrecord ClosingData [date
                        open
                        high
                        low
                        close
                        volume
                        adj-close])

(def ^:dynamic *min-date* "the minimum date supported by yahoo" "1986-06-9")

;;NOTE Yahoo finance API is no longer free, using hardcoded csv data
;;(def ^:dynamic *yahoo-price-url* "http://chart.finance.yahoo.com/table.csv?s=%s&d=%s&e=%s&f=%s&g=d&a=%s&b=%s&c=%s&ignore=.csv")
(def ^:dynamic *yahoo-price-url* "dev-resources/public/AMZN-2010-2014.csv")

(defn- format-yahoo-url
  "format the url string for retrieving stock price data from yahoo.
  fmt - the url as a format string
  ticker - the stock ticker string
  smonth - start month (1 = January ... 12 = December), emonth - ending month
  sday - start day of month (1 ... 31), eday - ending day of month
  syear - start year (four digit integer), eyear - ending year
  "
  [ticker smonth sday syear emonth eday eyear]
  ;the 'month' parameters will be normalized by subtracting 1 from them since yahoo starts months from 0
  (format *yahoo-price-url* ticker (dec emonth) eday eyear (dec smonth) sday syear))

(defn- normalize-yahoo-data
  "normalize the csv price data returned from yahoo into a coll of closing data and return it in ascending order by date.
  (csv data is returned from yahoo in descending order by date.)"
  [csv-data]
  (reduce (fn [accum v]
            (let [data-map (zipmap [:date :open :high :low :close :volume :adj-close] (map utils/parse-string v))]
                  ;closing-data (map->ClosingData data-map)

              (conj accum data-map))) '() (rest csv-data)))

(defn- get-daily-prices
  "retrieves daily closing price data from yahoo. returns a parsed csv as a seq of vectors containing historical
  price data. First vector of the seq is the header: 'Date' 'Open' 'High' 'Low' 'Close' 'Volume' 'Adj Close'"
  [ticker start-date end-date]
  (let [{smonth :month sday :day syear :year} (utils/datetime-map (utils/str->joda start-date "yyyy-MM-dd"))
        {emonth :month eday :day eyear :year} (utils/datetime-map (utils/str->joda end-date "yyyy-MM-dd"))
        url (format-yahoo-url ticker smonth sday syear emonth eday eyear)
        _ (println "YAHOO URL: " url)
        csv (csv/parse-csv (slurp url))
        _ (println (utils/log-data csv))]
    (csv/parse-csv (slurp url))))

(def ^:private get-daily-prices-memo (memoize get-daily-prices))

(defn get-prices
  "get daily stock prices for the specified ticker, between start-date and end-date inclusive. The dates should be
  strings in yyyy-MM-dd format."
  ([ticker] (get-prices ticker *min-date* (utils/current-day-str)))
  ([ticker start-date] (get-prices ticker start-date (utils/current-day-str)))
  ([ticker start-date end-date]
   (let [csv (get-daily-prices-memo ticker start-date end-date)]
     (vec (normalize-yahoo-data csv)))))