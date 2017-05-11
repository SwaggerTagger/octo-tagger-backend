package jobs

import java.util.UUID

import akka.actor.Actor.Receive
import akka.actor.{ Actor, ActorRef }
import com.google.inject.Inject
import com.google.inject.name.Named
import utils.actors.SSEPublisher
import play.Logger
/**
 * Created by John on 11.05.2017.
 */
class SSEDebug @Inject() (
  @Named("sse-publisher") ssepublish: ActorRef
) extends Actor {
  override def receive: Receive = {
    case SSEDebug.OnStart => {
      Logger.info("Sending debug Classification Start")
      ssepublish ! SSEPublisher.ClassificationStart(UUID.fromString("1298ae9d-1b2b-4465-b81c-27e230091ff7"))
    }
  }
}
object SSEDebug {
  case object OnStart
}
