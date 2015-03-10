(defproject kmodel "0.3.0"
  :description "Example webapp using kafka as a source of truth"
  :url "https://github.com/pyr/kmodel"
  :main kmodel.main
  :dependencies [[org.clojure/clojure            "1.6.0"]
                 [org.clojure/tools.logging      "0.3.1"]
                 [compojure                      "1.3.2"]
                 [ring/ring-json                 "0.3.1"]
                 [cc.qbits/jet                   "0.5.7"]
                 [com.taoensso/carmine           "2.9.0"]
                 [org.spootnik/logconfig         "0.7.3"]
                 [org.apache.kafka/kafka-clients "0.8.2.0"]
                 [org.apache.kafka/kafka_2.10    "0.8.2.0"]])
