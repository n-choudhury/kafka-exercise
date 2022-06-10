package example.kafka

import config.Meta.{bootStrapServers, kafkaTopic}
import org.apache.kafka.clients.consumer.KafkaConsumer

import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters.IterableHasAsScala

object MessageConsumer {

  val props = new Properties()
  props.put("bootstrap.servers", bootStrapServers)
  props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  props.put("auto.offset.reset", "latest")
  props.put("group.id", "consumer-group")

  val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)


  def main(args: Array[String]): Unit = {
    val topic = if(args.length == 0) kafkaTopic else args(0)
    consumeFromKafka(topic)
  }


  def consumeFromKafka(topic: String): Unit = {

    consumer.subscribe(java.util.Collections.singletonList(topic))
    while (true) {
      val record = consumer.poll(Duration.ofSeconds(1000)).asScala
      for (data <- record.iterator)
        println(data.value())
    }
  }
}
