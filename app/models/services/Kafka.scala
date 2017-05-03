package models.services

import java.util.UUID
import javax.inject.{ Inject, Singleton }

import akka.kafka.scaladsl.{ Consumer, Producer }
import akka.kafka.{ ConsumerSettings, Subscriptions }
import akka.stream.scaladsl.{ Sink, Source }
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import play.Logger
import play.api.Configuration

import scala.util.{ Failure, Success, Try }

trait Kafka {
  def sink: Try[Sink[ProducerRecord[String, String], _]]
  def source(topic: String): Try[Source[ConsumerRecord[String, String], _]]
}

@Singleton
class KafkaImpl @Inject() (configuration: Configuration) extends Kafka {

  import akka.kafka.ProducerSettings
  import org.apache.kafka.common.serialization.StringSerializer

  def maybeKafkaUrl[K](f: String => K): Try[K] = {
    configuration.getString("kafka.url").fold[Try[K]] {
      Failure(new Error("kafka.url was not set"))
    } { kafkaUrl =>
      Success(f(kafkaUrl))
    }
  }

  def producerSettings: Try[ProducerSettings[String, String]] = {
    maybeKafkaUrl { kafkaUrl =>
      val serializer = new StringSerializer()
      val config = configuration.getConfig("akka.kafka.producer").getOrElse(Configuration.empty)
      ProducerSettings[String, String](config.underlying, serializer, serializer).withBootstrapServers(kafkaUrl)
    }
  }

  def consumerSettings: Try[ConsumerSettings[String, String]] = {
    maybeKafkaUrl { kafkaUrl =>
      val deserializer = new StringDeserializer()
      val config = configuration.getConfig("akka.kafka.consumer").getOrElse(Configuration.empty)
      ConsumerSettings(config.underlying, deserializer, deserializer)
        .withBootstrapServers(kafkaUrl)
        .withGroupId(UUID.randomUUID().toString)
    }
  }

  def sink: Try[Sink[ProducerRecord[String, String], _]] = {
    producerSettings.map(Producer.plainSink(_))
  }

  def source(topic: String): Try[Source[ConsumerRecord[String, String], _]] = {
    val subscriptions = Subscriptions.topics(topic)
    consumerSettings.map(Consumer.plainSource(_, subscriptions))
  }

}