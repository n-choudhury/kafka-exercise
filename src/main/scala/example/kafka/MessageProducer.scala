package example.kafka

import config.Meta.{bootStrapServers, kafkaTopic}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.time.LocalDateTime
import java.util.Properties

object MessageProducer {
  def main(args: Array[String]): Unit = {
    val topic = if(args.length == 0) kafkaTopic else args(0)
    def datetimeNow = LocalDateTime.now().toString
    publishToKafka(topic, s"Message from Kafka Producer at $datetimeNow")
  }

  def publishToKafka(topic: String, messageStr: String) = {
    val properties = new Properties()

    properties.put("bootstrap.servers", bootStrapServers)
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](properties)
    val record = new ProducerRecord[String, String](topic, "key", messageStr)
    producer.send(record)
    producer.close()
  }
}
