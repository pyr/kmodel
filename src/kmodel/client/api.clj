(ns kmodel.client.api
  "HTTP API to serve our entities."
  (:require [compojure.route      :refer [resources not-found]]
            [compojure.core       :refer [routes GET POST DELETE PUT]]
            [ring.util.response   :refer [response redirect]]
            [kmodel.client.db     :refer [add! del! all]]
            [ring.middleware.json :as json]))

(defn api-routes
  "Serve our JSON API and static content, nothing fancy."
  [db]
  (->
   (routes
    (GET    "/api/job"     []           (response (all db)))
    (POST   "/api/job"     req          (response (add! db (:body req))))
    (PUT    "/api/job/:id" [id :as req] (response (add! db id (:body req))))
    (DELETE "/api/job/:id" [id]         (response (del! db id)))
    (GET    "/"            []           (redirect "/index.html"))

    (resources                           "/")
    (not-found                           "<html><h2>404</h2></html>"))

   (json/wrap-json-body {:keywords? true})
   (json/wrap-json-response)))
