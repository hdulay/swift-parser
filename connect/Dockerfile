#
# Copyright 2020 Confluent Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM confluentinc/cp-server-connect-base:6.1.1

ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components"

ARG CONNECTOR_NAME
ARG CONNECTOR_VERSION
RUN curl -O https://repo1.maven.org/maven2/com/ibm/mq/com.ibm.mq.allclient/9.2.2.0/com.ibm.mq.allclient-9.2.2.0.jar
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:10.1.0
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-ibmmq:11.0.2
# RUN cp com.ibm.mq.allclient-9.2.2.0.jar /usr/share/confluent-hub-components/confluentinc-kafka-connect-ibmmq/lib/
COPY --chown=appuser:appuser ibmmq/*.jar /usr/share/confluent-hub-components/confluentinc-kafka-connect-ibmmq/lib/
COPY --chown=appuser:appuser oracle/*.jar /usr/share/confluent-hub-components/confluentinc-kafka-connect-jdbc/lib/
# COPY oracle/ojdbc7.jar /usr/share/confluent-hub-components/confluentinc-kafka-connect-jdbc/lib/
