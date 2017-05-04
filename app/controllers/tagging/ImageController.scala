package controllers.tagging

import java.util.UUID
import javax.inject.Inject

import akka.actor.ActorRef
import akka.util.{ ByteString, Timeout }
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.Silhouette
import models.daos.{ ImageDAO, PredictionDAO }
import models.{ Prediction, TaggingImage }
import play.api.http.Writeable
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import akka.pattern.ask
import utils.actors.{ KafkaWriteActor, TagImageActor }
import utils.auth.DefaultEnv
import utils.azure.BlobStorage
import utils.json.JsonFormats

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

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
  def getImageMimeType(filename: String) = filename.toLowerCase match {
    case n if (n.endsWith(".jpeg") || n.endsWith(".jpg")) => Some("image/jpeg")
    case n if n.endsWith(".png") => Some("image/png")
    case _ => None
  }

  implicit val jsonPrettyWritable = new Writeable[JsValue]((value) => ByteString(Json.prettyPrint(value)), Some("application/json"))
  implicit val timeout = Timeout(30.seconds)

  def uploadImage = silhouette.SecuredAction.async(parse.multipartFormData) { request =>
    request.body.file("picture") match {
      case Some(file) => getImageMimeType(file.filename) match {
        case Some(mimeType) =>
          val result = (tagImageActor ?
            TagImageActor.TagImage(file.ref.file, mimeType, request.identity.userID)).mapTo[TaggingImage]

          val resolved = Await.result(result, 60.seconds)

          kafkaWriteActor ! KafkaWriteActor.QueuePrediction(resolved)
          Future.successful(Ok(Json.writes[TaggingImage].writes(resolved)))

        case _ => Future.successful(BadRequest("Unsupported file type"))
      }
      case _ => Future.successful(BadRequest("You must supply the filename/extension with the X-Filename header"))
    }
  }

  def listImages = silhouette.SecuredAction.async { request =>
    for {
      images: Seq[TaggingImage] <- imageDAO.listOwnImages(request.identity.userID)
      predictions: Seq[Prediction] <- predictionDAO.getPredictions(images.map(_.imageId))
    } yield Ok(JsonFormats.writeImageswithPredicitions(images, predictions))
    //imageDAO.listOwnImages(request.identity.userID).map(images => Ok(Json.toJson(images).toString()))
  }

  def deleteImage(imageId: UUID) = silhouette.SecuredAction.async { request =>
    for {
      url <- imageDAO.delete(imageId, request.identity.userID)
      _ <- blobStorage.delete(url)
    } yield Ok
  }
}
