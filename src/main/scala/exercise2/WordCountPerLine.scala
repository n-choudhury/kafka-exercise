package exercise2

import akka.actor.{ActorSystem, Props}
import config.Meta.kafkaTopic
import exercise2.WordCountActor.Subscribe

object WordCountPerLine {
  val actorSystem = ActorSystem("Word-Count-System")
  val wordCounter = actorSystem.actorOf(Props[WordCountActor], s"word-count-actor-${System.currentTimeMillis()}")

  def main(args: Array[String]): Unit = {
    val topic = if (args.length == 0) kafkaTopic else args(0)
    wordCounter ! Subscribe(topic)
  }
}
