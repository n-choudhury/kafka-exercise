package exercise1

import akka.actor.{ActorSystem, Props}
import config.Meta.kafkaTopic
import exercise1.MultiChannelActor.Subscribe

object EntryPoint {
  val actorSystem = ActorSystem("Consumer-System")
  val multiChannelActor = actorSystem.actorOf(Props[MultiChannelActor], s"multichannel-actor-${System.currentTimeMillis()}")

  def main(args: Array[String]): Unit = {
    val topic = if (args.length == 0) kafkaTopic else args(0)
    multiChannelActor ! Subscribe(topic)
  }
}
