package controllers.auth

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ PasswordHasherRegistry, PasswordInfo }
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.{ WebJarAssets, auth }
import models.services.{ AuthTokenService, UserService }
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, Controller }
import utils.auth.DefaultEnv
import utils.json.JsonFormats

import scala.concurrent.Future

/**
 * The `Reset Password` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository.
 * @param passwordHasherRegistry The password hasher registry.
 * @param authTokenService       The auth token service implementation.
 * @param webJarAssets           The WebJar assets locator.
 */
class ResetPasswordController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  passwordHasherRegistry: PasswordHasherRegistry,
  authTokenService: AuthTokenService,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  /**
   * Views the `Reset Password` page.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def view(token: UUID): Action[AnyContent] = silhouette.UnsecuredAction.async { implicit request =>
    authTokenService.validate(token).map {
      case Some(authToken) => Redirect(s"/#/resetPassword/$token")
      case None => Redirect("/#/resetPassword/invalidToken")
    }
  }

  case class ResetPasswordData(password: String)
  implicit val passwordRead = Json.format[ResetPasswordData]
  /**
   * Resets the password.
   *
   * @param token The token to identify a user.
   * @return The result to display.
   */
  def submit(token: UUID): Action[ResetPasswordData] = silhouette.UnsecuredAction.async(JsonFormats.validateJson[ResetPasswordData]) { implicit request =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) =>
        userService.retrieve(authToken.userID).flatMap {
          case Some(user) if user.loginInfo.providerID == CredentialsProvider.ID =>
            val passwordInfo = passwordHasherRegistry.current.hash(request.body.password)
            authInfoRepository.update[PasswordInfo](user.loginInfo, passwordInfo).map { _ =>
              Redirect("/#/resetPassword/success")
            }
          case _ => Future.successful(NotFound(Json.obj("error" -> Messages("invalid.reset.link"))))
        }

      case None => Future.successful(NotFound(Json.obj("error" -> Messages("invalid.reset.link"))))
    }
  }
}
