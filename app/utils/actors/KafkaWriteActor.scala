package utils.actors

import akka.actor.Actor
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.google.inject.Inject
import models.TaggingImage
import models.services.Kafka
import org.apache.kafka.clients.producer.ProducerRecord
import play.Logger
import utils.actors.KafkaWriteActor.QueuePrediction

class KafkaWriteActor @Inject() (kafka: Kafka) extends Actor {

  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case QueuePrediction(taggingImage) =>
      kafka.sink.map { msg =>
        Logger.info("This shit into kafka yo:")
        Source(0 to 1)
          .map(_ => new ProducerRecord[String, String]("incoming-pics", taggingImage.toString))
          .to(msg)
          .run()
      }
  }
}

object KafkaWriteActor {
  case class QueuePrediction(taggingImage: TaggingImage)
}
