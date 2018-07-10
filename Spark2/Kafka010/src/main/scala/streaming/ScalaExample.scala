package streaming
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._
import scala.collection.mutable.HashMap

object ScalaExample {
  def main(args: Array[String])= {
    if( args.length != 5){
      println("Please input <yarn-client/cluster/local> <broker list> <topic set> <groupID> <protocol> ")
      System.exit(1)
    }

    val Array(mode,brokerList,topic,groupId,protocol) = args

    val conf = new SparkConf().setAppName("ScalaExample").setMaster(mode)
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(5))

    val kafkaParams = new HashMap[String, Object]()
    kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerList)
    kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG,groupId)
    kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    kafkaParams.put("security.protocol", protocol)
    kafkaParams.put("auto.offset.reset", "earliest")

    val topicSet = topic.split(",").map(_.trim).filter(!_.isEmpty).toSet

    val consumerStrategy = ConsumerStrategies.Subscribe[String, String](topicSet, kafkaParams)
    val stream = KafkaUtils.createDirectStream(
      ssc, LocationStrategies.PreferBrokers, consumerStrategy)

    val words = stream.flatMap { r => r.value().split(" ") }.map { r => (r, 1) }.reduceByKey(_ + _)
    words.print
    ssc.start()
    ssc.awaitTermination()
  }
}
