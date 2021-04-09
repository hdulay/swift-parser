# Building this example without docker

1. Create a clustr in Confluent Cloud.
1. Goto [docs.confluent.io](https://docs.confluent.io/platform/current/installation/installing_cp/zip-tar.html) and follow the instructions to download Confluent Platform archive. You will only be using the connect cluster that comes with Confluent Platform.
1. Install the connectors. Run these commands from CP home.  `confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:10.1.0` and `confluent-hub install --no-prompt confluentinc/kafka-connect-ibmmq:11.0.2`.
`
1. In Confluent Cloud, goto `CLI and Tools/Kafka Connect` to build a `connect distrubuted configuration`. Select `Requires enterprise license`. Click on `Generate config`.

