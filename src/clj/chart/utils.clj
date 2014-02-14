(ns chart.utils
  (:require [clojure.string :as str]
            [clj-time.format :as jf]
            [clj-time.core :as jt]
            [clj-time.local :as jl]))


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
  [s date-fmt]
  (jf/parse (jf/formatter date-fmt) s))

(defn str->numeric
  "parse string data into an int or double, if possible, otherwise return the original string"
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

(defn file-names
  "get names of files in a directory, filter names by suffix"
  [dir suffix]
  (filter #(.endsWith % suffix) (.list dir)))

;TO rename keys in a map (map #(clojure.set/rename-keys % {:date :x :price :y}) pdmap)
