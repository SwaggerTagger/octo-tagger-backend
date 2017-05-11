package controllers.tagging

import javax.inject.Inject

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.OverflowStrategy
import akka.stream.impl.ActorPublisher
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.util.ByteString
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.Silhouette
import play.api.http.{ ContentTypes, HttpEntity, Writeable }
import play.api.i18n.MessagesApi
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, AnyContent, Controller }
import play.mvc.Http
import utils.actors.SSEPublisher.ImageTaggingEvent
import utils.auth.DefaultEnv
import utils.json.JsonFormats
import play.Logger
import utils.Helper
import utils.Helper.SSEvent
import utils.actors.SSEEventBus
/**
 * Created by John on 11.05.2017.
 */
class EventController @Inject() (
  eventbus: SSEEventBus,
  system: ActorSystem,
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv])
  extends Controller {
  //implicit val jsonPrettyWritable = new Writeable[JsValue]((value) => ByteString(Json.prettyPrint(value)), Some("application/json"))

  def stream: Action[AnyContent] = silhouette.SecuredAction { implicit req =>

    val source = Source.actorRef[ImageTaggingEvent](Int.MaxValue, OverflowStrategy.fail).mapMaterializedValue(ref => eventbus.subscribe(ref, req.identity.userID)).map(event => {
      Logger.debug(s"Sending event for image: ${event.image.imageId} to user: ${event.image.ownedBy}")
      Helper.convertEventToByteString(SSEvent(None, Some(event.eventType), Some(Json.obj("image" -> JsonFormats.writeImageswithPredictions(event.image, event.predictions)).toString)))
    })
    Ok.chunked(source).as(ContentTypes.EVENT_STREAM)
  }
}
