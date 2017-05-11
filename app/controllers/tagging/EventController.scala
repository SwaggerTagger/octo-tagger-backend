package controllers.tagging

import javax.inject.Inject

import akka.actor.ActorRef
import akka.stream.impl.ActorPublisher
import akka.stream.scaladsl.Source
import akka.util.{ ByteString, Timeout }
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.Silhouette
import models.SSEToken
import models.daos.{ ImageDAO, PredictionDAO, SSETokenDAO }
import play.api.http.{ HttpEntity, Writeable }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, AnyContent, Controller }
import play.mvc.Http
import utils.actors.SSEPublisher.SSEEvent
import utils.auth.DefaultEnv
import utils.azure.BlobStorage
import utils.json.JsonFormats

/**
 * Created by John on 11.05.2017.
 */
class EventController @Inject() (
  @Named("sse-publisher") ssePublisherRef: ActorRef,
  sseTokenDao: SSETokenDAO,
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv])
  extends Controller {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  implicit val jsonPrettyWritable = new Writeable[JsValue]((value) => ByteString(Json.prettyPrint(value)), Some("application/json"))
  val ssePublisher = ActorPublisher[SSEEvent](ssePublisherRef)

  def streamtoken: Action[AnyContent] = silhouette.SecuredAction.async { implicit req =>
    sseTokenDao.createToken(req.identity.userID).map(token => Ok(Json.writes[SSEToken].writes(token)))
  }

  def stream(tokenString: String): Action[AnyContent] = Action.async { implicit req =>
    sseTokenDao.getToken(tokenString).map {
      case Some(token) => {
        val source = Source.fromPublisher(ssePublisher).filter(_.image.ownedBy == token.userId).map(event =>
          ByteString(Json.obj("type" -> event.eventType, "image" -> JsonFormats.writeImageswithPredictions(event.image, event.predictions)).toString)
        )
        Ok.sendEntity(HttpEntity.Streamed(source, None, Some(Http.MimeTypes.JSON)))
      }
      case None => Unauthorized("Please obtain a token first")
    }

  }
}
