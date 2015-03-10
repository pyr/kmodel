(ns kmodel.main
  "Glue namespace to set-up the environment and
  call-out to the correct main function."
  (:gen-class)
  (:require [clojure.edn   :as edn]
            [kmodel.client :as client]
            [kmodel.worker :as worker]))

(def serializer
  "Default string serializer"
  "org.apache.kafka.common.serialization.StringSerializer")

(def defaults
  "Default configuration"
  {:http     {:port 8080}
   :redis    {:pool {}
              :spec {:host "127.0.0.1"
                     :port 6379}}
   :producer {:bootstrap.servers "localhost:9092"
              :value.serializer  serializer
              :key.serializer    serializer}
   :consumer {:zookeeper.connect "localhost:2181"
              :group.id          "job-db"}})

(defn err-out!
  "Something bad happened, bail out."
  [& args]
  (binding [*out* *err*]
    (apply println args)
    (System/exit 1)))

(defn usage!
  "Hint at what should be done."
  []
  (err-out! "Usage: kmodel {client|worker} [config-file]"))

(defn read-conf
  "Read an EDN configuration file"
  [path]
  (try
    (-> path slurp edn/read-string)
    (catch Exception e
      (err-out! "Cannot parse configuration: " (str e)))))

(defn -main
  "Our main entry point, load "
  [& [mode path]]
  (let [opts (merge defaults (if path (read-conf path) {}))]
    (cond
      (= mode "client") (client/start opts)
      (= mode "worker") (worker/stream->db opts)
      :else             (usage!))))
