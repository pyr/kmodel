kmodel: Proof of Concept Kafka materialized view
================================================

This demo project showcases the use of [Apache Kafka](http://kafka.apache.org)
to create materialized views from a stream of events. This sample projects assumes you have
read the following articles:

- http://engineering.linkedin.com/distributed-systems/log-what-every-software-engineer-should-know-about-real-time-datas-unifying
- http://kafka.apache.org/documentation.html#compaction

The application holds a single entity type: a job description for a fictive
job board. A list of all published job descriptions is also held.

The app is split in three components:

- A stateless API which reads off of redis and schedules writes through kafka.
- A stateless consumer which reads the event stream and keeps redis up to date.
- A small single-page javascript application to interact with the API.

## Building

In the project directory, run `lein uberjar`, a resulting standalone artifact
will be in `target/kmodel-0.3.0-standalone.jar`

## Running

Start the client by running `java -jar target/kmodel-0.3.0-standalone.jar client`,
or the worker by running `java -jar target/kmodel-0.3.0-standalone.jar worker`.

If you want to test modifications, you can run the project with leiningen as well,
by issuing either `lein run -- client` or `lein run -- worker`.

Additionally, a path pointing to an EDN configuration file may be supplied as the
last argument.

## Configuration format

The defaults are shown here and assume both redis, zookeeper and kafka are
available locally, adapt as needed.

```clojure
  {:http     {:port 8080}
   :redis    {:pool {}
              :spec {:host "127.0.0.1"
                     :port 6379}}
   :producer {:bootstrap.servers "localhost:9092"
              :value.serializer  serializer
              :key.serializer    serializer}
   :consumer {:zookeeper.connect "localhost:2181"
              :group.id          "job-db"}}
```

## Prerequisites

You will of course need both zookeeper and kafka running, as well as
a reachable redis instance.

You will need to create the kafka topic and indicate in its configuration that
log compaction is enabled:

```
./bin/kafka-topics.sh --create --topic=job --zookeeper=localhost:2181 --partitions=10 --replication-factor=1 --config=cleanup.policy=compact
```

You will also need the following set in your kafka broker properties:

```
log.cleaner.enable=true
```


## Code organization

The code is split across the following namespaces:

- `kmodel.main`: glue namespace, to start things
- `kmodel.worker`: kafka to redis consumer
- `kmodel.client`: glue namespace for the client
- `kmodel.client.db`: persistence layer implementation
- `kmodel.client.api`: HTTP API implementation

## API Routes

- `GET /api/job`: return jobs
- `POST /api/job`: insert a new job
- `PUT /api/job/:id`: update a job by ID
- `DELETE /api/job/:id`: delete job by ID
