package controllers.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Credentials, PasswordHasherRegistry, PasswordInfo }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.services.UserService
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import utils.auth.{ DefaultEnv, WithProvider }
import utils.json.JsonFormats

/**
 * The `Change Password` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param credentialsProvider    The credentials provider.
 * @param authInfoRepository     The auth info repository.
 * @param passwordHasherRegistry The password hasher registry.
 */
class ChangePasswordController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  credentialsProvider: CredentialsProvider,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry)
  extends Controller with I18nSupport {

  case class ChangePasswordData(currentPassword: String, newPassword: String)
  implicit val changePasswordReads: Reads[ChangePasswordData] = (
    (JsPath \ "currentPassword").read[String] and (JsPath \ "newPassword").read[String]
  )(ChangePasswordData.apply _)

  /**
   * Changes the password.
   *
   * @return The result to display.
   */
  def submit = silhouette.SecuredAction(WithProvider[DefaultEnv#A](CredentialsProvider.ID)).async(JsonFormats.validateJson[ChangePasswordData]) { implicit request =>
    val ChangePasswordData(currentPassword, newPassword) = request.body
    val credentials = Credentials(request.identity.email.getOrElse(""), currentPassword)
    credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
      val passwordInfo = passwordHasherRegistry.current.hash(newPassword)
      authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
        NoContent
      }
    }.recover {
      case e: ProviderException =>
        Unauthorized(Json.obj("error" -> Messages("current.password.invalid")))
    }

  }
}
