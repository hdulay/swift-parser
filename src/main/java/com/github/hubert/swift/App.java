package com.github.hubert.swift;

import com.prowidesoftware.swift.model.field.Field;
import com.prowidesoftware.swift.model.field.Field20;
import com.prowidesoftware.swift.model.field.Field32A;
import com.prowidesoftware.swift.model.mt.mt1xx.MT103;
import com.prowidesoftware.swift.utils.Lib;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 *
 */
public class App {
    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "swift-parser-example");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "swift-parser-example");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);

        KStream<String, GenericRecord> swiftMessages = builder.stream("ibmmq"); // input topic

        KStream<String, GenericRecord> data = swiftMessages.mapValues((k, v) -> {

            String raw = v.get("text").toString();

            /*
             * Read and parse the file content into a SWIFT message object
             * Parse from File could also be used here
             */
            try(ByteArrayInputStream bais = new ByteArrayInputStream(raw.getBytes())) {
                MT103 mt = MT103.parse(Lib.readStream(bais, null));
                GenericRecord mt103 = getGenericRecord(k, mt, raw);
                return mt103;
            } catch (IOException e) {
                e.printStackTrace();

                // you should create an error avro message and populate it with error details
                // and the original message and return it. you can "branch" it to route these
                // messages to an error topic.
                return v;
            }
        });

        data.print(Printed.toSysOut());

        // Message routing using branch method
        KStream<String, GenericRecord>[] branches = data.branch(
                (key, value) -> (value.get("raw") == null), // missing the raw field, then must be error
                (key, value) -> true
        );

        // Branches can have different sink topics
        branches[0].to("errors"); // error topic
        branches[1].to("mt103"); // output topic

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        streams.setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
        });

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    /**
     * Construct the AVRO generic record from the swift message. AVRO will help downstream consumers
     * to make integration a lot easier.
     *
     * @param key - kafka key
     * @param mt - the swift message object
     * @param raw - the raw swift message
     * @return the generic avro record
     * @throws IOException - if the avro schema could not be read
     */
    private static GenericRecord getGenericRecord(String key, MT103 mt, String raw) throws IOException {
        try(InputStream is = App.class.getClassLoader().getResourceAsStream("mt103.avsc")) {
            String schemaStr = new String(IOUtils.toByteArray(is));

            Schema.Parser parser = new Schema.Parser();
            Schema schema = parser.parse(schemaStr);
            GenericRecord avroRecord = new GenericData.Record(schema);

            avroRecord.put("id", key);
            avroRecord.put("sender", mt.getSender());
            avroRecord.put("receiver", mt.getReceiver());

            Field20 ref = mt.getField20();
            avroRecord.put("Field20", Field.getLabel(ref.getName(), mt.getMessageType(), null) + ": " +
                    ref.getComponent(Field20.REFERENCE));

            Field32A f = mt.getField32A();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            avroRecord.put("value_date", f.getDateAsCalendar().getTimeInMillis());

            avroRecord.put("amount", f.getCurrency()+" "+f.getAmount());

            avroRecord.put("raw", raw.substring(0,255));
            return avroRecord;
        }
    }
}
