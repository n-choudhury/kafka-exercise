package exercise1

import EntryPoint._
import akka.actor.{Actor, ActorSystem, Props}
import example.kafka.{ConsumerActor, ProducerActor}
import example.kafka.ConsumerActor.{ConsumeFromKafka, ConsumedMessage}
import example.kafka.ProducerActor.PublishToKafka

object MultiChannelActor {
  case class Subscribe(topic: String)
  val producerActor = actorSystem.actorOf(Props[ProducerActor], s"producer-actor-${System.currentTimeMillis()}")
  val consumerActor = actorSystem.actorOf(Props[ConsumerActor], s"consumer-actor-${System.currentTimeMillis()}")
}

class MultiChannelActor extends Actor {
  import MultiChannelActor._

  override def receive: Receive = {
    case Subscribe(topic) => consumerActor ! ConsumeFromKafka(topic)

    case ConsumedMessage(topic, message) =>
      if (message.nonEmpty) {
        val newTopic = message
        val newMessage = message.replace(' ', '-').toUpperCase

        producerActor ! PublishToKafka(newTopic, newMessage)
      }
  }
}
