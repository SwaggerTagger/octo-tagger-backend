package jobs

import akka.actor.Actor
import com.google.inject.Inject
import jobs.KafkaJob.OnStart
import models.daos.SSETokenDAO
import play.Logger
/**
 * Created by John on 11.05.2017.
 */
class SSETokenCleaner @Inject() (
  sseTokenDao: SSETokenDAO
) extends Actor {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  override def receive: Receive = {
    case OnStart => {
      Logger.debug("Starting to clean SSE-Tokens")
      val f = sseTokenDao.cleanToken()
      f onSuccess {
        case count if count > 0 => Logger.debug(s"Didn't find any SSE-Tokens to clean")
        case count => Logger.info(s"Cleaned $count expired tokens")
      }
      f onFailure {
        case e => Logger.error("Error cleaning expired tokens", e)
      }
    }
  }
}
object SSETokenCleaner {
  case object OnStart
}