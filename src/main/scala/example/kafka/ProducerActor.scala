package example.kafka

import akka.actor.Actor
import config.Meta.producerProps
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object ProducerActor {
  case class PublishToKafka(topic: String, message: String)
  var producer: KafkaProducer[String, String] = null
}

class ProducerActor extends Actor {
  import ProducerActor._

  override def preStart(): Unit = {
    producer = new KafkaProducer[String, String](producerProps)
  }

  override def receive: Receive = {
    case PublishToKafka(topic, messageStr) =>
      val record = new ProducerRecord[String, String](topic, "key", messageStr)
      producer.send(record)
  }

  override def postStop(): Unit = {
    producer.close()
  }
}
