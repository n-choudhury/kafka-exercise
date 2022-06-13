package config

import java.util.Properties

object Meta {

  val kafkaTopic: String = "maintopic"

  val kafkaBootStrapServers: String = "localhost:9092"
  val kafkaConsumerGroup: String = "consumer-group"

  val producerProps = new Properties()
  producerProps.put("bootstrap.servers", kafkaBootStrapServers)
  producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val consumerProps = new Properties()
  consumerProps.put("bootstrap.servers", kafkaBootStrapServers)
  consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
  consumerProps.put("auto.offset.reset", "latest")
  consumerProps.put("group.id", kafkaConsumerGroup)
}
