package controllers.tagging

import java.util.UUID
import javax.inject.Inject

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.impl.ActorPublisher
import akka.stream.scaladsl.Source
import akka.util.{ ByteString, Timeout }
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.Silhouette
import com.sksamuel.scrimage.ImageParseException
import models.daos.{ ImageDAO, PredictionDAO }
import models.{ Prediction, TaggingImage }
import play.Logger
import play.api.http.{ HttpEntity, Writeable }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.iteratee.{ Concurrent, Enumerator }
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import play.mvc.Http
import utils.actors.SSEPublisher.ImageTaggingEvent
import utils.actors.{ KafkaWriteActor, TagImageActor }
import utils.auth.DefaultEnv
import utils.azure.BlobStorage
import utils.exceptions.HttpError
import utils.json.JsonFormats

import scala.concurrent.Future
import scala.concurrent.duration._

class ImageController @Inject() (
  blobStorage: BlobStorage,
  imageDAO: ImageDAO,
  @Named("tag-image-actor") tagImageActor: ActorRef,
  @Named("kafka-write-actor") kafkaWriteActor: ActorRef,
  predictionDAO: PredictionDAO,
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv])
  extends Controller with I18nSupport {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  implicit val jsonPrettyWritable = new Writeable[JsValue]((value) => ByteString(Json.prettyPrint(value)), Some("application/json"))
  implicit val timeout = Timeout(30.seconds)
  def uploadImage = silhouette.SecuredAction.async(parse.multipartFormData) { request =>
    request.body.file("picture") match {
      case Some(file) =>

        ((tagImageActor ?
          TagImageActor.TagImage(file.ref.file, request.identity.userID, file.filename)) recoverWith {
            case e: Exception => {
              Logger.error("Error when uploading", e)
              Future.failed(e)
            }
          }
        ).mapTo[Either[Exception, TaggingImage]].map {
            case Left(_: ImageParseException) => throw new HttpError(play.api.mvc.Results.UnsupportedMediaType("Unsupported file type"))
            case Left(e) => throw e
            case Right(image) => {
              kafkaWriteActor ! KafkaWriteActor.QueuePrediction(image)
              Created(Json.writes[TaggingImage].writes(image))
            }
          }

      case _ => Future.successful(BadRequest("You must supply an image for this request"))
    }
  }

  def listImages = silhouette.SecuredAction.async { request =>
    for {
      images: Seq[TaggingImage] <- imageDAO.listOwnImages(request.identity.userID)
      predictions: Seq[Prediction] <- predictionDAO.getPredictions(images.map(_.imageId))
    } yield Ok(JsonFormats.writeImageswithPredicitions(images, predictions))
  }

  def deleteImage(imageId: UUID) = silhouette.SecuredAction.async { request =>
    for {
      url <- imageDAO.delete(imageId, request.identity.userID)
      _ <- blobStorage.delete(url)
    } yield NoContent
  }

}
