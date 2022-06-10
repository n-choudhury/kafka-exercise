package config

import java.util.Properties

object Meta {

  val kafkaTopic: String = "maintopic"

  val bootStrapServers: String = "localhost:9092"

  val producerProps = new Properties()
  producerProps.put("bootstrap.servers", bootStrapServers)
  producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val consumerProps = new Properties()
  consumerProps.put("bootstrap.servers", bootStrapServers)
  consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  consumerProps.put("auto.offset.reset", "latest")
  consumerProps.put("group.id", "consumer-group")
}
