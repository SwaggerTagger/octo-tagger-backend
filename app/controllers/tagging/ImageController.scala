package controllers.tagging

import java.io.File
import javax.inject.Inject

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.{ HandlerResult, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import controllers.WebJarAssets
import models.TaggingImage
import models.daos.ImageDAO
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Controller, Request }
import utils.auth.DefaultEnv
import utils.azure.BlobStorage

import scala.concurrent.Future

/**
 * Created by jlzie on 14.04.2017.
 */
class ImageController @Inject() (
  blobStorage: BlobStorage,
  imageDAO: ImageDAO,
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv])
  extends Controller with I18nSupport {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  def getImageMimeType(filename: String) = filename.toLowerCase match {
    case n if (n.endsWith(".jpeg") || n.endsWith(".jpg")) => Some("image/jpeg")
    case n if n.endsWith(".png") => Some("image/png")
    case _ => None
  }

  def uploadImage = silhouette.SecuredAction.async(parse.temporaryFile) { request =>
    //request.body.moveTo(new File("/tmp/picture"))

    request.headers.get("X-Filename") match {
      case Some(f) => getImageMimeType(f) match {
        case Some(t) => for {
          (url, date) <- blobStorage.upload(request.body.file, t)
          image <- imageDAO.create(url, date, request.identity.userID)
        } yield Ok(Json.writes[TaggingImage].writes(image))

        case _ => Future.successful(BadRequest("Unsupported file type"))
      }
      case _ => Future.successful(BadRequest("You must supply the filename/extension with the X-Filename header"))
    }

  }
  def listImages = silhouette.SecuredAction.async {

    Future.successful(Ok("Test"))
  }
}
