(ns scratch.scrapes
  (:require [net.cgrand.enlive-html :as html]))

;; test scrape of briefing.com (new) earnings format
(def ^:dynamic *earnings-path* "dev-resources/public/earnings/ibm.html")
(def ^:dynamic *earnings-selector* [:table.search-results #{:tr.rD :tr.rL} :td :> :div])

(defn scrape-data
  "use enlive to scrape earnings data for each quarter and partition it into groups. The Briefing.com earnings page
  provides 11 pieces of earnings data (see the EarningsData record)"
  [path]
  (partition 11 (html/select (html/html-resource (java.io.File. path)) *earnings-selector*)))
