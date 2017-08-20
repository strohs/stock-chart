(ns chart.datasources.yahoo
  (:require [clojure-csv.core :as csv]
            [chart.utils :as utils]
            [clj-http.client :as client]
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

;;get prices for last four years
(def ^:dynamic *min-date* "the minimum date supported by yahoo" "2009-01-01")

;;NOTE Yahoo finance API is no longer free, using hardcoded csv data
;;(def ^:dynamic *yahoo-price-url* "http://chart.finance.yahoo.com/table.csv?s=%s&d=%s&e=%s&f=%s&g=d&a=%s&b=%s&c=%s&ignore=.csv")
(def ^:dynamic *yahoo-price-url* "dev-resources/public/AMZN-2010-2014.csv")

;(defn- format-yahoo-url
;  "format the url string for retrieving stock price data from yahoo.
;  fmt - the url as a format string
;  ticker - the stock ticker string
;  smonth - start month (1 = January ... 12 = December), emonth - ending month
;  sday - start day of month (1 ... 31), eday - ending day of month
;  syear - start year (four digit integer), eyear - ending year
;  "
;  [ticker smonth sday syear emonth eday eyear]
;  ;the 'month' parameters will be normalized by subtracting 1 from them since yahoo starts months from 0
;  (format *yahoo-price-url* ticker (dec emonth) eday eyear (dec smonth) sday syear))

;;crumb regex string hidden in yahoo finance response body
(def crumb-regex #"CrumbStore\":\{\"crumb\":\"(.+?)\"\}")

(defn- get-b-cookie
  "get yahoo bcookie thats required now in order to use the Finance API"
  [resp] (get-in resp [:cookies "B" :value]))

(defn- get-crumb
  "get 'crumb' string form response, its also required now along by Finance API along with the b-cookie"
  [resp] (get (re-find crumb-regex (:body resp)) 1))

(defn get-daily-prices-csv
  "retrieves daily closing prices as .csv string. This function uses the new Yahoo Fianance API 'cookie scheme'
  whereby Yahoo now requires you to pass a cookie"
  [symbol start-date end-date]
  (let [yahoo-url (format "https://finance.yahoo.com/quote/%s/history" symbol)
        _ (println "URL:" yahoo-url)
        yahoo-resp (client/get yahoo-url)
        _ (println yahoo-resp)
        b-cookie (get-b-cookie yahoo-resp)
        _ (println "BCookie:" b-cookie)
        crumb (get-crumb yahoo-resp)
        _ (println "crumb:" crumb)
        period1 (utils/str->epoch start-date)
        period2 (utils/str->epoch end-date)
        final-csv-url (format "https://query1.finance.yahoo.com/v7/finance/download/%s?period1=%s&period2=%s&interval=1d&events=history&crumb=%s" symbol period1 period2 crumb)
        _ (println final-csv-url)
        csv-resp (client/get final-csv-url
                             {:cookies {"B" {:value b-cookie}}})]
        ;_ (println csv-resp)]
    (:body csv-resp)))

(defn- normalize-yahoo-data
  "normalize the csv price data returned from yahoo into a clojure map and return it in ascending order by date.
  Normalizing means converting numerical Strings into their numerical form and  parsing dates into yyyy-MM-dd format
  (csv data is returned from yahoo in descending order by date.)"
  [csv-data]
  (reduce (fn [accum v]
            (let [data-map (zipmap [:date :open :high :low :close :adj-close :volume] (map utils/parse-string v))]
                  ;closing-data (map->ClosingData data-map)

              (conj accum data-map))) '() (rest csv-data)))

;(defn- get-daily-prices
;  "retrieves daily closing price data from yahoo. returns a parsed csv as a seq of vectors containing historical
;  price data. First vector of the seq is the header: 'Date' 'Open' 'High' 'Low' 'Close' 'Volume' 'Adj Close'"
;  [ticker start-date end-date]
;  (let [{smonth :month sday :day syear :year} (utils/datetime-map (utils/str->joda start-date "yyyy-MM-dd"))
;        {emonth :month eday :day eyear :year} (utils/datetime-map (utils/str->joda end-date "yyyy-MM-dd"))
;        url (format-yahoo-url ticker smonth sday syear emonth eday eyear)
;        _ (println "YAHOO URL: " url)
;        csv (csv/parse-csv (slurp url))
;        _ (println (utils/log-data csv))]
;    (csv/parse-csv (slurp url))))

(defn- get-daily-prices
  "retrieves daily closing price data from yahoo. returns a parsed csv as a seq of vectors containing historical
  price data. First vector of the seq is the header: 'Date' 'Open' 'High' 'Low' 'Close' 'Adj Close' 'Volume' "
  [ticker start-date end-date]
  (let [raw-csv (get-daily-prices-csv ticker start-date end-date)
        ;_ (println "CSV: " raw-csv)
        csv (csv/parse-csv raw-csv)  ;this call parses out the header
        _ (println (utils/log-data csv))]
    (csv/parse-csv raw-csv)))

(def ^:private get-daily-prices-memo (memoize get-daily-prices))

(defn get-prices
  "get daily stock prices for the specified ticker, between start-date and end-date inclusive. The dates should be
  strings in yyyy-MM-dd format."
  ([ticker] (get-prices ticker *min-date* (utils/current-day-str)))
  ([ticker start-date] (get-prices ticker start-date (utils/current-day-str)))
  ([ticker start-date end-date]
   (let [csv (get-daily-prices-memo ticker start-date end-date)]
     (vec (normalize-yahoo-data csv)))))