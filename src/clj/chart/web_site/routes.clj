(ns chart.web-site.routes
  (:use compojure.core
        ring.util.json-response
        chart.core
        chart.utils)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [ring.util.response :as resp]))

;(defn json-response [data & [status]]
;  {:status  (or status 200)
;   :headers {"Content-Type" "application/json"}
;   ;:body (json/generate-string data)
;   })


(defroutes main-routes
           (GET "/" [] (resp/redirect "/index.html"))
           (GET "/data/:ticker/:date/:before/:after" [ticker date before after]
                (json-response (chart-day-range ticker date (parse-string before) (parse-string after))))
           (GET "/earnings/:ticker/:date" [ticker date]
                (json-response
                  (let [earnings-dates (chart-earnings-range ticker date)]
                    earnings-dates)))
           ;(GET "/a" [] (resp/resource-response "/index.html" {:root "public"}))
           ;(GET "/f" [] (clojure.java.io/resource "public/index.html"))
           (route/resources "/"))


;(defroutes api-routes
;           (GET "/user/:id" [id] (str "you hit the user route: " id))
;           (GET "/json/:id" [id] (json-response {"fookey" id :cljkey "clj1234"}))
;           )



(def app
  (-> (routes main-routes (route/not-found "Page not found"))
      (handler/api)))
