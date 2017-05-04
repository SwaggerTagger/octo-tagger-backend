package utils.actors

import akka.actor.Actor
import akka.actor.FSM.Failure
import akka.actor.Status.Success
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.google.inject.Inject
import models.TaggingImage
import models.services.Kafka
import org.apache.kafka.clients.producer.ProducerRecord
import play.Logger
import play.api.libs.json.Json
import utils.actors.KafkaWriteActor.QueuePrediction

import scala.concurrent.Future
import scala.util.Try

class KafkaWriteActor @Inject() (kafka: Kafka) extends Actor {

  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case QueuePrediction(taggingImage) =>
      kafka.sink
        .map { msg =>
          Logger.info(s"Writing TaggingImage[id=${taggingImage.imageId}] into Kafka")
          val result = Source(0 to 0)
            .map(_ => new ProducerRecord[String, String](
              "incoming-pics",
              taggingImage.imageId.toString,
              Json.writes[TaggingImage].writes(taggingImage).toString()))
            .to(msg)
            .run()
        }
  }
}

object KafkaWriteActor {
  case class QueuePrediction(taggingImage: TaggingImage)
}
