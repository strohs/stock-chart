(ns chart.web-site.routes
  (:use compojure.core
        ring.util.json-response)
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
           (GET "/a" [] (resp/resource-response "/index.html" {:root "public"}))
           (GET "/f" [] (clojure.java.io/resource "public/index.html"))
           (route/resources "/"))


(defroutes api-routes
           (GET "/user/:id" [id] (str "you hit the user route: " id))
           (GET "/json/:id" [id] (json-response {"fookey" id :cljkey "clj1234"}))
           )



(def app
  (-> (routes main-routes api-routes (route/not-found "Page not found"))
      (handler/site)))
