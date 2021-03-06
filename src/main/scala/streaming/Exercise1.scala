package streaming

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
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

  val kafkaProducerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers(kafkaBootStrapServers)

  def readFromKafka(topic: String) = {
    val kafkaConsumerSettings: ConsumerSettings[String, String] = ConsumerSettings(actorSystem, stringDeserializer, stringDeserializer)
      .withBootstrapServers(kafkaBootStrapServers)
      .withGroupId(kafkaConsumerGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withStopTimeout(0.seconds)

    val control /*: Consumer.DrainingControl[Done] */ = Consumer
      .committableSource(kafkaConsumerSettings, Subscriptions.topics(topic))
      .map { consumerMssg =>
        val mssgVal = consumerMssg.record.value()
        println(s"Received Message: $mssgVal on topic: $topic")
        val newTopic = mssgVal.replace(' ', '-')
        val newMessage = mssgVal.toUpperCase
        ProducerMessage.single(
          new ProducerRecord(newTopic, consumerMssg.record.key(), newMessage),
          passThrough = consumerMssg.committableOffset)
      }
      .via(Producer.flexiFlow(kafkaProducerSettings))
      .map(_.passThrough)
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
