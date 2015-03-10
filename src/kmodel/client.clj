(ns kmodel.client
  "Startup a persistence instance and serve the API from it."
  (:require [qbits.jet.server  :refer [run-jetty]]
            [kmodel.client.db  :as db]
            [kmodel.client.api :as api]))

(defn start
  "Glue persistence, API handler and web server together."
  [opts]
  (let [db   (db/redis-kafka-db opts)
        api  (api/api-routes db)]
    (run-jetty (-> opts :http (assoc :ring-handler api)))))
