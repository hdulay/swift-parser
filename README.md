# swift-parser

```text
                                                   +--------------+
+------------+                                     |              |
|            |                                     |              |
|            |         +-----------------+         |  Kafka       |
|  IBMMQ     | +-----> | IBMMQ Connector | +---->  |              |
|            |         |                 |         |              |      +--------------+
|            |         +-----------------+         |              | +--> |              |
+------------+                                     |              |      | KStreams     |
                                                   |              |      | Swift        |
                                                   |              | <--+ | Parse/Route  |
                                                   |              |      |              |
 +------------+        +-----------------+         |              |      +--------------+
 |            |        | JDBC Sink       |         |              |
 |            | <----+ |                 | <----+  |              |
 | Oracle     |        +-----------------+         |              |
 | RDS        |                                    |              |
 |            |                                    |              |
 +------------+                                    |              |
                                                   +--------------+

```

## Prerequisite - downlaod oracle jdbc driver & ibmmq client jars

Please download a oracle jdbc driver and create/place it into **connect/oracle** directory. The jar will be placed into the connector container so that the jdbc sink connecter can locate it.

Likewise, download the IBM MQ client jars and create/place them into `connect/ibmmq` directory so the IBM MQ source connector can load them.

This demo uses `docker`. Please have it installed as well as `docker-compose`.

1. Go to Confluent Cloud and create a Kafka cluster.
1. Install the `ccloud` cli. Instructions are in the Confluent Cloud UI.
1. Install `jq` to parse json responses from rest commands.
1. Install maven (`mvn`) tool.
1. Install sqlplus to connect to Oracle.

## Makefile

```Makefile
# DOCKER COMPOSE ENV VARIABLES
export BOOTSTRAP_SERVERS={{ BOOTSTRAP_SERVERS }}:9092
export SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule   required username='{{ CONFLUENT KEY }}'   password='{{ SECRET }}';
export SASL_JAAS_CONFIG_PROPERTY_FORMAT=org.apache.kafka.common.security.plain.PlainLoginModule   required username='{{ CONFLUENT KEY }}'   password='{{  SECRET }}';
export REPLICATOR_SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule   required username='{{ CONFLUENT KEY }}'   password='{{  SECRET }}';
export BASIC_AUTH_CREDENTIALS_SOURCE=USER_INFO
export SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO={{ SR KEY }}:{{ SR SECRET }}
export SCHEMA_REGISTRY_URL={{ SR URL }

# Creds
CLOUD_KEY={{ CONFLUENT KEY }}
CLOUD_SECRET={{ SECRET }}

# Confluent Cloud environment
ENV=
# Confluent Cloud cluster id
CLUSTER=
# Connect hostname ( localhost for docker )
CONNECT=localhost
```

Execute these commands. See the Makefile for details. This will build the docker images to pre-install the connectors (ibmmq & jdbc sink for oracle). This will also create a connect cluster into which you will deploy connector configurations.

```bash
make build
make cluster
# wait a minute for cluster to spinup
# you can run this command to check the status of the connect cluster before moving forward
make status
```

## Make the topics and connectors

Create the topics in you Confluent Cloud Kafka cluster. You will need `ccloud cli installed and be logged in`.

The connect configurations will be deployed into the connect cluster.

```bash
make topic
```

### Install Connectors

Follow instructions in Confluent Hub for JDBC Sink/Source connector and IBMMQ Source connector to install into your Kafka connect cluster.

## Open the IBM MQ Dashboard

[log into the IBMMQ console](https://localhost:9443/ibmmq/console/login.html) to send swift messages

```conf
UserName=admin
Password=passw0rd
```

## Oracle

This command uses sqlplus

```bash
make oracle
```

Create the table to which the jdbc sink connector will writee

```sql
CREATE TABLE "kafka_mt103" (
    "id" VARCHAR2(128) NOT NULL,
    "sender" VARCHAR2(128) NOT NULL,
    "receiver" VARCHAR2(128),
    "Field20" VARCHAR(128),
    "value_date" NUMBER(19,0),
    "amount" VARCHAR2(128),
    "raw" CLOB,
    PRIMARY KEY("id")
);

-- When messages arrive, run this statement
SELECT * FROM "{{ OWNER }}"."kafka_mt103";
```

## KStream application

Run the kstream swift parser/router. This section uses maven to run the application. You will need to modify the pom.xml to create an UBER jar to run as `java -jar`.

Create a configuration file

```properties
# KStream swift parser app
intopic=ibmmq
outtopic=mt103
errortopic=invalid
application.id=swift-parser
client.id=swift-parser-example

# Required connection configs for Kafka producer, consumer, and admin
bootstrap.servers={{ BOOTSTRAP_SERVERS }}
security.protocol=SASL_SSL
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule   required username='{{ CONFLUENT KEY }}'   password='{{  SECRET }}';
sasl.mechanism=PLAIN
# Required for correctness in Apache Kafka clients prior to 2.6
client.dns.lookup=use_all_dns_ips

# Best practice for Kafka producer to prevent data loss
acks=all

# Required connection configs for Confluent Cloud Schema Registry
schema.registry.url={{ SR URL }}
basic.auth.credentials.source=USER_INFO
basic.auth.user.info={{ SR KEY }}:{{ SR SECRET }}
```

```bash
make kstream
```



## Show AVRO schema

Goto the link below to view the AVRO schema the datagen connector registered to schema registry.
![clickstream schema](images/clickstream-schema.png)

You need to send a message to IBM MQ before the schema will appear in the topic in C3.

- Select `DEV.QUEUE.1` under "Queues on MQ1"

![ibmmq](images/ibmmq-queues.png)

- Add a message

![add image](images/addmessage.png)
![add image](images/addmessage2.png)

- You can now see the schema assigned to the `ibmmq` topic

![ibmmq topic](images/ibmmq-schema.png)
