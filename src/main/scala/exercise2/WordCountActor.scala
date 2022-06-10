package exercise2

import akka.actor.{Actor, Props}
import example.kafka.ConsumerActor.{ConsumeFromKafka, ConsumedMessage}
import example.kafka.ProducerActor.PublishToKafka
import example.kafka.{ConsumerActor, ProducerActor}
import exercise1.EntryPoint.actorSystem

object WordCountActor {

  case class Subscribe(topic: String)
  val producerActor = actorSystem.actorOf(Props[ProducerActor], s"producer-actor-${System.currentTimeMillis()}")
  val consumerActor = actorSystem.actorOf(Props[ConsumerActor], s"consumer-actor-${System.currentTimeMillis()}")
}

class WordCountActor extends Actor {
  import WordCountActor._
  override def receive: Receive = {
    case Subscribe(topic) => consumerActor ! ConsumeFromKafka(topic)

    case ConsumedMessage(topic, message) =>
      if (message.nonEmpty) {
        message.split("\\s").filterNot(_ == "").map(_ -> 1).groupBy(_._1).foreach { pair =>
          (pair._1, pair._2.length)
          val recrdKey: String = message.replace(' ', '-')
          val mssg: String = s"(${pair._1}, ${pair._2.length})"
          producerActor ! PublishToKafka(recrdKey, mssg)
        }
      }
  }
}
