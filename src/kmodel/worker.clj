(ns kmodel.worker
  "Kafka consumer used to materialize"
  (:require [taoensso.carmine      :as r]
            [clojure.tools.logging :refer [info error]])
  (:import java.util.Properties
           kafka.consumer.Consumer
           kafka.consumer.ConsumerConfig
           kafka.consumer.ConsumerIterator
           kafka.consumer.KafkaStream
           kafka.javaapi.consumer.ConsumerConnector))

(defn consumer-config
  "Kafka consumer config from configuration map."
  [opts]
  (let [props (Properties.)]
    (doseq [[k v] opts]
      (.put props (name k) (str v)))
    (ConsumerConfig. props)))

(defmulti materialize! "Dispatch on operation type." :op)

(defmethod materialize! :del
  [payload]
  (info "removing " (:key payload))
  (r/srem "jobs" (:key payload))
  (r/del (:key payload)))

(defmethod materialize! :set
  [payload]
  (info "updating " (:key payload))
  (r/set (:key payload) (:msg payload))
  (r/sadd "jobs" (:key payload)))

(defn stream->db
  "Single threaded, single topic consumer.
   This consumer obviously has a lot of shortcomings."
  [{:keys [redis kafka]}]
  (let [topic     "job"
        topic-map {topic (int 1)}
        cfg       (consumer-config kafka)
        consumer  (Consumer/createJavaConsumerConnector cfg)
        streams   (.createMessageStreams consumer topic-map)
        stream    (first (get streams topic))
        it        (.iterator stream)]
    (doseq [payload (iterator-seq it)]
      (try
        (let [key (String. (.key payload) "UTF-8")
              msg (when-let [m (.message payload)] (String. m "UTF-8"))
              op  (if (nil? msg) :del :set)]
          (r/wcar redis (materialize! {:op op :key key :msg msg})))
        (catch Exception e
          (error e "Cannot handle record with key" key))))))
