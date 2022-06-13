package streaming

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.Metadata.CommittedOffset
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{CommitterSettings, ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Committer, Consumer, Producer}
import akka.stream.scaladsl.{Keep, Sink, Source}
import config.Meta.{kafkaBootStrapServers, kafkaConsumerGroup, kafkaTopic}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
object Exercise2 {

  implicit val actorSystem: ActorSystem = ActorSystem("KafkaToElasticSearch")
  implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  val stringDeserializer = new StringDeserializer
  val stringSerializer = new StringSerializer

  val kafkaProducerSettings = ProducerSettings(actorSystem, new StringSerializer, new StringSerializer)
    .withBootstrapServers(kafkaBootStrapServers)


  def readFromKafkaToKafka(topic: String) = {
    val kafkaConsumerSettings: ConsumerSettings[String, String] = ConsumerSettings(actorSystem, stringDeserializer, stringDeserializer)
      .withBootstrapServers(kafkaBootStrapServers)
      .withGroupId(kafkaConsumerGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withStopTimeout(0.seconds)

    val control = Consumer
      .committableSource(kafkaConsumerSettings, Subscriptions.topics(topic))
      .map { consumerRecord =>
        val key = consumerRecord.record.key()
        val messageVal = consumerRecord.record.value()
        val producerRecords = messageVal.split("\\s").filterNot(_ == "").map(_ -> 1).groupBy(_._1).map { pair =>
          val newTopic = messageVal.replace(' ', '-')
          val mssg: String = s"(${pair._1}, ${pair._2.length})"
          new ProducerRecord[String, String](newTopic, key, mssg)
        }.toSeq
        ProducerMessage.multi(producerRecords, consumerRecord.committableOffset)
      }
      .via(Producer.flexiFlow(kafkaProducerSettings))
      .map(_.passThrough)
      .toMat(Committer.sink(CommitterSettings(actorSystem)))(Keep.both)
      .run()
  }

  def main(args: Array[String]): Unit = {
    val topic = if (args.length == 0) kafkaTopic else args(0)
    readFromKafkaToKafka(topic)
  }
}
