build:
	docker-compose build

cluster: ibmjars
	docker-compose up -d

ps:
	docker-compose ps

ibmjars:
	docker-compose up -d ibmmq
	-docker exec -it -e LICENSE=accept -e MQ_QMGR_NAME=MQ1 ibmmq cp -Ra /opt/mqm/java/lib/  /project/mqlibs

topic:
	docker exec -it connect kafka-topics --bootstrap-server broker:29092 --create --topic ibmmq --partitions 1 --replication-factor 1
	docker exec -it connect kafka-topics --bootstrap-server broker:29092 --create --topic mt103 --partitions 1 --replication-factor 1

connect:
	docker exec -it connect curl -d "@/ibmmq/ibmmq-connect.json" \
		-X PUT \
		-H "Content-Type: application/json" \
		http://connect:8083/connectors/ibmmq-source/config 

	docker exec -it connect curl -d "@/mysql/mysql-sink-connect.json" \
		-X PUT \
		-H "Content-Type: application/json" \
		http://connect:8083/connectors/mysql-mt103/config 

kstream:
	mvn clean package
	mvn exec:java -D"exec.mainClass"="com.github.hubert.swift.App" 1> /dev/null

down:
	docker-compose down
	-rm -rf mqlibs/*

consumer:
	docker exec -it connect kafka-avro-console-consumer --bootstrap-server broker:29092 --topic ibmmq --from-beginning \
		--property schema.registry.url=http://schema-registry:8081 \
		--property consumer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor

