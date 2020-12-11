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
 | MySQL      |        +-----------------+         |              |
 |            |                                    |              |
 |            |                                    |              |
 +------------+                                    |              |
                                                   +--------------+

```

## Prerequisite - downlaod mysql driver

Please download a mysql jdbc driver and place it into the mysql directory. The jar will be placed into the connector container so that the jdbc sink connecter can locate it.

## IBM MQ

IBM MQ is running in a docker container. IBM MQ source connector will read from it and send it to Confluent Platform.

## Make commands

Execute these commands. See the Makefile for details.

```bash
make build
make cluster
# wait a minute for cluster to spinup
```

## Make the topics and connectors

```bash
make topic
make connect
# wait a minute before moving on to the next step
```

## Open the IBM MQ Dashboard

[log in](https://localhost:9443/ibmmq/console/login.html)

```conf
UserName=admin
Password=passw0rd
```

## KStream application

Debug through the application using your IDE then place a swift message into the IBM MQ. Alternatively you can run the command below.

```bash
make kstream
```

## Show AVRO schema in C3 topics

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
