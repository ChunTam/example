package streaming;

import java.util.*;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.kafka010.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.streaming.*;

public class JavaExample {

    public static void main(String[] args){

        if(args.length != 5){
            System.out.println("Please input <yarn-client/cluster/local> <broker list> <topic set> <groupID> <protocol> ");
            System.exit(1);
        }

        String mode =  args[0];
        String brokerList = args[1];
        Collection<String> topics = Arrays.asList(args[2].split(","));
        String groupId = args[3];
        String protocol = args[4];


        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers",brokerList);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", groupId);
        kafkaParams.put("auto.offset.reset", "earliest");
        kafkaParams.put("security.protocol", protocol);

        SparkConf conf = new SparkConf().setMaster(mode).setAppName("NetworkWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));


        JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        jssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
                );

        stream.foreachRDD( x ->  x.foreach(record -> System.out.println(record.value()))); //print out the record
        //or i can do stream.print()?

        jssc.start();              // Start the computation

        try{
            jssc.awaitTermination();   // Wait for the computation to terminate
        }catch (InterruptedException e){
            System.err.println("InterruptedExcetpion: " + e.getMessage());
        }

    }


}