package utils.actors

import java.util.UUID
import javax.inject.Inject

import akka.actor.Props
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Cancel
import models.{ Prediction, TaggingImage }
import models.daos.{ ImageDAO, PredictionDAO }
import play.api.libs.json.{ JsValue, Json }
import utils.actors.SSEPublisher.{ ClassificationFinished, ClassificationStart, SSEEvent }

/**
 * Created by John on 11.05.2017.
 */
class SSEPublisher @Inject() (
  imageDAO: ImageDAO,
  predictionDAO: PredictionDAO) extends ActorPublisher[SSEEvent] {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  def receive = {
    case ClassificationStart(imageId) => sendEvent(imageId, "started")
    case ClassificationFinished(imageId) => sendEvent(imageId, "finished")
    case Cancel => context.stop(self)
    case _ =>
  }
  def sendEvent(imageId: UUID, eType: String) = {
    for {
      im <- imageDAO.getImage(imageId)
      predictions <- predictionDAO.getPredictions(Seq(imageId))
    } yield im match {
      case Some(image) => onNext(SSEEvent(image, predictions, eType))
      case None =>
    }

  }
}

object SSEPublisher {
  def props = Props[SSEPublisher]
  case class ClassificationStart(imageId: UUID)
  case class ClassificationFinished(imageId: UUID)
  case class SSEEvent(image: TaggingImage, predictions: Seq[Prediction], eventType: String)
}
