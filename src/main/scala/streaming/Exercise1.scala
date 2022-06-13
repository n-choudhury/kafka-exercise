package streaming

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.scaladsl.{Sink, Source}
import config.Meta.{kafkaBootStrapServers, kafkaConsumerGroup, kafkaTopic}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Exercise1 {
  implicit val actorSystem: ActorSystem = ActorSystem("KafkaToElasticSearch")
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  val stringDeserializer = new StringDeserializer
  val stringSerializer = new StringSerializer

  def writeToKafka(topic: String, messages: scala.collection.immutable.Iterable[String]) = {

    val kafkaProducerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
      .withBootstrapServers(kafkaBootStrapServers)

    val produce = Source(messages)
      .map { mssg =>
//        println(s"Producing mssg: $mssg to topic: $topic")
        new ProducerRecord(topic, "Key", mssg)
      }
      .runWith(Producer.plainSink(kafkaProducerSettings))
    produce
  }

  def readFromKafka(topic: String) = {
    val kafkaConsumerSettings: ConsumerSettings[String, String] = ConsumerSettings(actorSystem, stringDeserializer, stringDeserializer)
      .withBootstrapServers(kafkaBootStrapServers)
      .withGroupId(kafkaConsumerGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withStopTimeout(0.seconds)

    val control /*: Consumer.DrainingControl[Done] */ = Consumer
      .sourceWithOffsetContext(kafkaConsumerSettings, Subscriptions.topics(topic))
      .map { consumerRecord =>
        println(s"Received Message: ${consumerRecord.value()} on topic: $topic")
        val newTopic = consumerRecord.value().replace(' ', '-')
        val newMessage = consumerRecord.value().toUpperCase

        writeToKafka(newTopic, Seq(newMessage))
      }
      .runWith(Sink.ignore)

    control.onComplete {
      case Success(_) =>
        println("Successfully terminated consumer")
        actorSystem.terminate()

      case Failure(err) =>
        println(err.getMessage)
        actorSystem.terminate()
    }
  }


  def main(args: Array[String]): Unit = {
    val topic = if (args.length == 0) kafkaTopic else args(0)
    readFromKafka(topic)
  }
}
