package controllers.tagging

import java.io.File
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ HandlerResult, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import controllers.WebJarAssets
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
        case Some(t) => blobStorage.upload(request.body.file, t).map(url => {

          Ok(url)
        })
        case _ => Future.successful(BadRequest("Unsupported file type"))
      }
      case _ => Future.successful(BadRequest("You must supply the filename/extension with the X-Filename header"))
    }

  }
}
