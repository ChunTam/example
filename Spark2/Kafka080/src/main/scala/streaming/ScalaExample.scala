package streaming

  import org.apache.kafka.clients.consumer.ConsumerConfig
  import org.apache.kafka.common.serialization.StringDeserializer
import _root_.kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka._
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream._



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

    val kafkaParams = Map[String, String](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokerList,
      ConsumerConfig.GROUP_ID_CONFIG->groupId,
      "security.protocol"-> protocol,
    "auto.offset.reset" -> "earliest"
    )

    val topicSet = topic.split(",").map(_.trim).filter(!_.isEmpty).toSet
    //  def createDirectStream[K, V, KD <: kafka.serializer.Decoder[K], VD](ssc, kafkaParams : scala.Predef.Map[scala.Predef.String, scala.Predef.String], topics : scala.Predef.Set[scala.Predef.String])
    val stream = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc, kafkaParams, topicSet)

    val words = stream.flatMap { r => r._2.split(" ") }.map { r => (r, 1) }.reduceByKey(_ + _)
    words.print
    ssc.start()
    ssc.awaitTermination()
  }
}
