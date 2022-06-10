package example.kafka

import akka.actor.Actor
import config.Meta.consumerProps
import org.apache.kafka.clients.consumer.KafkaConsumer

import java.time.Duration
import scala.jdk.CollectionConverters.IterableHasAsScala

object ConsumerActor {
  case class ConsumeFromKafka(topic: String)
  case class ConsumedMessage(topic: String, mssg: String)
  val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](consumerProps)
}

class ConsumerActor extends Actor {
  import ConsumerActor._
  override def receive: Receive = {
    case ConsumeFromKafka(topic: String) =>
      consumer.subscribe(java.util.Collections.singletonList(topic))
      while (true) {
        val record = consumer.poll(Duration.ofSeconds(10)).asScala
        for (data <- record.iterator) {
          if (data.value().nonEmpty) {
            //          println(data.value())
            sender ! ConsumedMessage(topic, data.value())
          }
        }
      }

  }
}
