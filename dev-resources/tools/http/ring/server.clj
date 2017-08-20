;;; This namespace is used for development and testing purpose only.
(ns ring.server
  (:use chart.core
        chart.utils
        chart.datasources.yahoo)
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [net.cgrand.enlive-html :as enlive]
            [compojure.route :refer (resources)]
            [compojure.core :refer (GET POST defroutes)]
            [ring.util.response :as resp]
            [ring.middleware.params :refer (wrap-params)]
            [ring.util.json-response :refer (json-response)]
            [ring.adapter.jetty :as jetty]
            [clojure.java.io :as io]))

;;; We use enlive lib to add to the body of the index.html page the
;;; script tag containing the JS code which activates the bREPL
;;; connection.
(enlive/deftemplate page
                    (io/resource "public/index.html")
                    []
                    [:body] (enlive/append
                              (enlive/html [:script (browser-connected-repl-js)])))

(defroutes main-routes
           (GET "/" [] (page))
           (GET "/data/:ticker/:date/:before/:after" [ticker date before after]
                (json-response (chart-day-range ticker date (parse-string before) (parse-string after))))
           (GET "/earnings" [ticker date]
                (json-response (chart-earnings-range ticker date)))
           (GET "/earnings/:ticker/:date" [ticker date]
                (json-response (chart-earnings-range ticker date)))
           (GET "/ticker/list" []
                (let [names (file-names (io/as-file (io/resource "public/earnings")) "html")]
                  (json-response names)))
           ;(GET "/a" [] (resp/resource-response "/index.html" {:root "public"}))
           ;(GET "/f" [] (clojure.java.io/resource "public/index.html"))
           (resources "/"))

;wrap-params enable query sting params/ FORM parmeters to be passed into routing functions
(def site (wrap-params main-routes))



;;; defroutes macro defines a function that chains individual route
;;; functions together. The request map is passed to each function in
;;; turn, until a non-nil response is returned.
;(defroutes site
;           (resources "/")
;           (GET "/*" req (page)))

;;; To run the jetty server. The server symbol is not private to
;;; allows to start and stop thejetty server from the repl.
(defn run
  "Run the ring server. It defines the server symbol with defonce."
  []
  (defonce server
           (jetty/run-jetty #'site {:port 3000 :join? false}))
  server)
