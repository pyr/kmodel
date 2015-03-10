(ns kmodel.client.db
  "Our persistence interaction. All reads hit the materialized view
   stored in Redis, all writes go out to Kafka."
  (:require [taoensso.carmine :as r]
            [clojure.edn      :as edn])
  (:import org.apache.kafka.clients.producer.ProducerRecord
           org.apache.kafka.clients.producer.KafkaProducer
           java.util.Properties))

(defprotocol JobDB
  "Our persistence protocol."
  (add! [this payload] [this id payload] "Upsert entry.")
  (del! [this id] "Remove entry.")
  (all  [this] "Retrieve all entries."))

(defn id!
  "Yield a valid unique redis key."
  []
  (str "job:" (java.util.UUID/randomUUID)))

(defn record
  "Create a kafka record."
  [topic key data]
  (ProducerRecord. topic key (when data (pr-str data))))

(defn kafka-producer
  "Get a kafka producer from a configuration map."
  [opts]
  (let [props (Properties.)]
    (doseq [[k v] opts]
      (.put props (name k) (str v)))
    (KafkaProducer. props)))

(defn redis-kafka-db
  "Our implementation of JobDB which does reads in Redis and writes
   in Kafka."
  [{:keys [redis producer]}]
  (let [producer (kafka-producer producer)]
    (reify
      JobDB
      (all [this]
        (->> (for [k (r/wcar redis (r/smembers "jobs"))]
               [k (edn/read-string (r/wcar redis (r/get k)))])
             (reduce merge {})))
      (add! [this id payload]
        (.send producer (record "job" id payload))
        {})
      (add! [this payload]
        (add! this (id!) payload))
      (del! [this id]
        (.send producer (record "job" id nil))
        {}))))
