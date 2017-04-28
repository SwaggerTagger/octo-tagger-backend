package utils.exceptions

/**
 * Created by jlzie on 27.04.2017.
 */
import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._

@Singleton
class HttpErrorHandler @Inject() (
  env: Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: Provider[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onServerError(request: RequestHeader, exception: Throwable) = {
    exception match {
      case HttpError(result) => Future.successful(result)
      case _ => super.onServerError(request, exception)
    }
  }

}
