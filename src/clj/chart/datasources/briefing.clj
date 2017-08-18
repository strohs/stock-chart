(ns chart.datasources.briefing
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [chart.utils :as utils]
            [clj-time.core :as jt]))

;namespace for retrieving earnings data from a briefing.com (payservice) earnings page. Briefing.com does not provide
; a api to retrieve this data, so it must be scraped from a saved HTML pages ... for now.

(def ^:dynamic *earnings-path* "dev-resources/public/earnings")

;data that can be returned from briefing.com earnings page
(defrecord EarningsData [release-date
                         release-date-joda
                         fiscal-end-date
                         fiscal-quarter
                         release-time
                         eps-surprise
                         eps-actual
                         eps-consensus
                         eps-1year-change
                         rev-actual
                         rev-consensus
                         rev-y2y])


;(def ^:dynamic *earnings-date-selector* [:table :tr :td :table.search-results #{:tr.rD :tr.rL} [html/first-child :td] :div [html/text-node]])
;;selects all earnings data, including: date, actual, consensus, etc.. as reported by briefing.com
;; (def ^:dynamic *earnings-selector* [:table :tr :td :table.search-results #{:tr.rD :tr.rL} :td :> :div])
;; new earnings selector as of Oct-01-2014
(def ^:dynamic *earnings-selector* [:table.search-results #{:tr.rD :tr.rL} :td :> :div])

(defn- scrape-data
  "use enlive to scrape earnings data for each quarter and partition it into groups. The Briefing.com earnings page
  provides 11 pieces of earnings data (see the EarningsData record)"
  [path]
  (partition 11 (html/select (html/html-resource (java.io.File. path)) *earnings-selector*)))

(defn- clean
  "clean-up string data that might be filled with garbage due to HTML scraping"
  [s]
  (if (or (nil? s) (= (first s) \newline))
    nil
    (str/replace s #"\n| |%" "")))


(defn- extract
  "extract node data into a map"
  [nodes]
  (let [data (map #(utils/str->numeric (clean (first (:content %)))) nodes)
        mapped-data (zipmap [:release-date :fiscal-end-date :fiscal-quarter :release-time :eps-surprise :eps-actual :eps-consenus :eps-1year-change :rev-actual :rev-consensus :rev-y2y]
                            data)
        mapped-data (update-in mapped-data [:release-date] utils/reformat "dd-MMM-yy" "yyyy-MM-dd")]
    ;convert release-date into a joda date as it will be used heavily later on
    (assoc mapped-data :release-date-joda (utils/str->joda (:release-date mapped-data)))))

(defn- months->quarters
  "build a map of release-date months to fiscal quarters. This map will be used to repair any missing
  fiscal quarter data that was not sent in the html scrape. Month indices are 1 based"
  [coll]
  (reduce (fn [qm m]
            (let [month (jt/month (get m :release-date-joda))
                  quarter (get m :fiscal-quarter)]
              (if quarter
                (update-in qm [month] conj quarter)
                qm)))
          {1 #{} 2 #{} 3 #{} 4 #{} 5 #{} 6 #{} 7 #{} 8 #{} 9 #{} 10 #{} 11 #{} 12 #{}} ;map of months to quarters
          coll))

(defn- repair-quarters
  "helper function that tries to fill in any missing :fiscal-quarter data that wasn't provided by the html scrape.
  It does this by looking at previous release dates and their corresponding quarters to fill in the missing quarter."
  [coll]
  (let [qmap (months->quarters coll)]
    (reduce (fn [accum m]
              (let [quarter (get m :fiscal-quarter)
                    month (jt/month (get m :release-date-joda))]
                (conj accum
                      (if (nil? quarter)
                        (assoc m :fiscal-quarter (first (qmap month)))
                        m))))
            [] coll)))

(defn get-earnings
  "get all earnings available and return a coll of EarningsData (sorted by release-date)"
  [path]
  (let [scraped (scrape-data path) ;returns coll of maps (scraped by enlive)
        _ (println "scraped: " (utils/log-data scraped))
        normalized (repair-quarters (map extract scraped))] ;try to fix any missing quarter data
                                   ;earnings-data (map #(map->EarningsData %) normalized)

    (sort-by :release-date-joda normalized)))

(defn parse-earnings [ticker]
  (let [epath (str *earnings-path* "/" ticker ".html")]
    (get-earnings epath)))

;TODO earnings util functions start here. Move to different namespace
(defn same-quarter
  "return historical earnings data with the same quarter as release-date. It will try to guess it based on previous
  quarters/release-date information"
  [earnings date]
  (let [release-date (utils/str->joda date)
        qmap (months->quarters earnings)
        month (jt/month release-date)
        quarter (first (qmap month))]
    (filter #(= quarter (:fiscal-quarter %)) earnings)))

(defn earnings-for-qtr
  "return earnings data for a specific quarter"
  [earnings qtr]
  (filter #(= qtr (:fiscal-quarter %)) earnings))

(defn earnings-for-year
  "return earnings data for a specific year"
  [earnings year]
  (filter #(= year (jt/year (:release-date-joda %))) earnings))



