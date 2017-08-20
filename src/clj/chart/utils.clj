(ns chart.utils
  (:require [clojure.string :as str]
            [clj-time.format :as jf]
            [clj-time.core :as jt]
            [clj-time.coerce :as jc]
            [clj-time.local :as jl]
            [clojure.java.io :refer [file]]))

;;; Utilities for converting string to and from JODA dates


(defn parse-string
  "Attempts to parse s into one of three types.  It first attempts to parse into a joda DateTime.
  If that fails it will attempt to parse into an Integer. If that fails attempt to parse into a Double.
  If all parsing attempts fail, returns the original string."
  [s]
  (cond
    (nil? s) s
    (re-matches #"\d{4}-\d{1,2}-\d{1,2}" s) (jf/parse (jf/formatter "yyyy-MM-dd") s)
    (re-matches #"-?\d+" s) (Integer/parseInt s)
    (re-matches #"-?\d+\.\d+" s) (Double/parseDouble s)
    :else s))


(defn str->joda
  "parse the string s into a Joda DateTime object using the specified date-fmt string."
  ([s] (jf/parse (jf/formatter "yyyy-MM-dd") s))
  ([s date-fmt] (jf/parse (jf/formatter date-fmt) s)))

(defn str->numeric
  "parse string data into an int or double if possible, otherwise return the original string"
  [s]
  (cond
    (nil? s) s
    (re-matches #"-?\d+" s) (Integer/parseInt s)
    (re-matches #"-?\d+\.\d+" s) (Double/parseDouble s)
    :else s))

(defn joda->str
  "unparse the joda DateTime (dt) into a string according to the format string (fmt)"
  [dt fmt]
  (jf/unparse (jf/formatter-local fmt) dt))

(defn str->epoch
  "convert a String in yyyy-MM-dd format to a unix epoch time
  returns - a long containing the unix epoch time"
  [s]
  ;;have to set time-zone offset to -5 for east coast time
  (jc/to-epoch (jt/from-time-zone (str->joda s) (jt/time-zone-for-offset -5))))


(defn reformat
  "reformat a date string into a different format. returns a string formatted in 'to-fmt'"
  [date-str from-fmt to-fmt]
  (joda->str (str->joda date-str from-fmt) to-fmt))

(defn current-day-str [] (jf/unparse-local-date (jf/formatters :year-month-day) (jt/today)))

(defn datetime-map
  "split the :month :day and :year fields of a joda DateTime (dt) into a clojure map"
  [dt]
  {:day (jt/day dt) :month (jt/month dt) :year (jt/year dt)})

(defn within?
  "is a joda date within start date and end date"
  [date start end] (jt/within? (jt/interval start end) date))

(defn slice
  "slice a vector from start-idx to end-idx (exclusive)"
  [v start-idx end-idx]
  (let [indexes (set (range start-idx end-idx))]
    (vec (keep-indexed #(if (contains? indexes %1) %2) v))))

(defn find-index
  "find the index of the first matching item in the coll."
  [coll item]
  (ffirst (filter #(= item (second %)) (map-indexed vector coll))))

(defn file-names
  "get names of files in a directory, does not include the file suffix"
  [dir suffix]
  (map #(re-find #"\w+" %)
       (filter #(.endsWith % suffix) (.list dir))))

(defn contains-date? [price-data date]
  (let [date (str->joda date)]
    (some #(= (:date %) date) price-data)))

(defn log-data [data]
  (str "count:" (count data) " first:" (first data) " last:" (last data)))

;TO rename keys in a map (map #(clojure.set/rename-keys % {:date :x :price :y}) pdmap)
