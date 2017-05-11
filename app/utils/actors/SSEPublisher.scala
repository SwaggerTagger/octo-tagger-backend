package utils.actors

import java.util.UUID
import javax.inject.Inject

import akka.actor.{ Actor, Props }
import akka.event.{ ActorEventBus, EventBus, LookupClassification }
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.Cancel
import com.google.inject.Singleton
import models.daos.{ ImageDAO, PredictionDAO }
import models.{ Prediction, TaggingImage }
import utils.actors.SSEPublisher.{ ClassificationFinished, ClassificationStart, ImageTaggingEvent }
import play.Logger
/**
 * Created by John on 11.05.2017.
 */
class SSEPublisher @Inject() (
  eventbus: SSEEventBus,
  imageDAO: ImageDAO,
  predictionDAO: PredictionDAO) extends ActorPublisher[ImageTaggingEvent] {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  def receive = {
    case ClassificationStart(imageId) => sendEvent(imageId, "started")
    case ClassificationFinished(imageId) => sendEvent(imageId, "finished")
    case _ =>
  }
  def sendEvent(imageId: UUID, eType: String) = {
    Logger.debug("received message")
    for {
      im <- imageDAO.getImage(imageId)
      predictions <- predictionDAO.getPredictions(Seq(imageId))
    } yield im match {
      case Some(image) => eventbus.publish(ImageTaggingEvent(image, predictions, eType))
      case None =>
    }

  }
}

object SSEPublisher {
  def props = Props[SSEPublisher]
  case class ClassificationStart(imageId: UUID)
  case class ClassificationFinished(imageId: UUID)
  case class ImageTaggingEvent(image: TaggingImage, predictions: Seq[Prediction], eventType: String)
}

@Singleton
class SSEEventBus extends EventBus with LookupClassification
  with ActorEventBus {

  type Event = ImageTaggingEvent
  type Classifier = UUID

  override def mapSize = 2

  override def classify(event: ImageTaggingEvent) = event.image.ownedBy

  override protected def publish(event: ImageTaggingEvent, subscriber: Subscriber) = subscriber ! event
}

class SSESubscriber extends Actor {
  override def receive: Receive = {
    case ImageTaggingEvent =>
  }
}

object SSESubscriber {
  def props = Props[SSESubscriber]
}
