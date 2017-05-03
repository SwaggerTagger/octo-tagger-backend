package controllers.tagging

import java.util.UUID
import javax.inject.Inject

import akka.util.ByteString
import com.mohiva.play.silhouette.api.Silhouette
import models.daos.{ ImageDAO, PredictionDAO }
import models.{ Prediction, TaggingImage }
import play.api.http.Writeable
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import utils.auth.DefaultEnv
import utils.azure.BlobStorage
import utils.json.JsonFormats

import scala.concurrent.Future

/**
 * Created by jlzie on 14.04.2017.
 */
class ImageController @Inject() (
  blobStorage: BlobStorage,
  imageDAO: ImageDAO,
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

  def uploadImage = silhouette.SecuredAction.async(parse.temporaryFile) { request =>
    request.headers.get("X-Filename") match {
      case Some(f) => getImageMimeType(f) match {
        case Some(t) => for {
          (height, width) <- utils.images.ImageHelper.getImageDimensions(request.body.file)
          (url, date) <- blobStorage.upload(request.body.file, t)
          image <- imageDAO.create(url, date, request.identity.userID, height, width)
        } yield Ok(Json.writes[TaggingImage].writes(image))

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
