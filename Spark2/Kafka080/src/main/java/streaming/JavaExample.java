package streaming;

import kafka.serializer.StringDecoder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.*;

import java.util.*;

public class JavaExample {

    public static void main(String[] args){

        if(args.length != 5){
            System.out.println("Please input <yarn-client/cluster/local> <broker list> <topic set> <groupID> <protocol> ");
            System.exit(1);
        }

        String mode =  args[0];
        String brokerList = args[1];
        Set<String> topics = new HashSet<>(Arrays.asList(args[2].split(",")));
        String groupId = args[3];
        String protocol = args[4];


        Map<String, String> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers",brokerList);
        kafkaParams.put("group.id", groupId);
        kafkaParams.put("auto.offset.reset", "smallest");
        kafkaParams.put("security.protocol", protocol);

        SparkConf conf = new SparkConf().setMaster(mode).setAppName("NetworkWordCount");
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));


        JavaPairDStream<String, String> stream =
                KafkaUtils.createDirectStream(
                        jssc, String.class, String.class, StringDecoder.class, StringDecoder.class,kafkaParams, topics
                );

        stream.foreachRDD( x ->  x.foreach(record -> System.out.println(record._2()))); //print out the record
        //or i can do stream.print()?

        jssc.start();              // Start the computation

        try{
            jssc.awaitTermination();   // Wait for the computation to terminate
        }catch (InterruptedException e){
            System.err.println("InterruptedExcetpion: " + e.getMessage());
        }

    }
}
