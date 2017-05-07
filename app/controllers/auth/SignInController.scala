package controllers.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import models.{ Prediction, User }
import models.services.UserService
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ JsPath, JsValue, Json, Writes }
import play.api.mvc.{ Call, Controller }
import utils.auth.DefaultEnv
import utils.json.JsonFormats

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * The `Sign In` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param credentialsProvider    The credentials provider.
 * @param socialProviderRegistry The social provider registry.
 * @param configuration          The Play configuration.
 * @param clock                  The clock instance.
 */
class SignInController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock)
  extends Controller with I18nSupport {
  case class SignInData(password: String, email: String)
  implicit object UserFormat extends Writes[User] {
    override def writes(o: User): JsValue = Json.obj("firstName" -> o.firstName, "lastname" -> o.lastName, "fullName" -> o.fullName, "avatarUrl" -> o.avatarURL)
  }
  implicit val signInRead = Json.format[SignInData]
  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async(JsonFormats.validateJson[SignInData]) { implicit request =>

    val credentials = Credentials(request.body.email, request.body.password)
    credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
      val result = NoContent
      userService.retrieve(loginInfo).flatMap {
        case Some(user) if !user.activated =>
          Future.successful(PreconditionFailed(Json.obj("error" -> "Please validate your email address")))
        case Some(user) =>
          silhouette.env.authenticatorService.create(loginInfo).flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
              silhouette.env.authenticatorService.embed(v, Ok(UserFormat.writes(user)))
            }
          }
        case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
      }
    }.recover {
      case e: ProviderException =>
        Unauthorized(Json.obj("error" -> "Wrong password/username"))
    }

  }
}
