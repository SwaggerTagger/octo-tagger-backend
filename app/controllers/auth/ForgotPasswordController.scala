package controllers.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import controllers.auth
import models.services.{ AuthTokenService, UserService }
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.Controller
import utils.auth.DefaultEnv
import utils.json.JsonFormats

import scala.concurrent.Future

/**
 * The `Forgot Password` controller.
 *
 * @param messagesApi      The Play messages API.
 * @param silhouette       The Silhouette stack.
 * @param userService      The user service implementation.
 * @param authTokenService The auth token service implementation.
 * @param mailerClient     The mailer client.
 */
class ForgotPasswordController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authTokenService: AuthTokenService,
  mailerClient: MailerClient)
  extends Controller with I18nSupport {
  case class ForgotPasswordData(email: String)
  implicit val forgotPasswordRead = Json.format[ForgotPasswordData]
  /**
   * Sends an email with password reset instructions.
   *
   * It sends an email to the given address if it exists in the database. Otherwise we do not show the user
   * a notice for not existing email addresses to prevent the leak of existing email addresses.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async(JsonFormats.validateJson[ForgotPasswordData]) { implicit request =>

    val loginInfo = LoginInfo(CredentialsProvider.ID, request.body.email)
    val result = NoContent
    userService.retrieve(loginInfo).flatMap {
      case Some(user) if user.email.isDefined =>
        authTokenService.create(user.userID).map { authToken =>
          val url = auth.routes.ResetPasswordController.view(authToken.id).absoluteURL()

          mailerClient.send(Email(
            subject = Messages("email.reset.password.subject"),
            from = Messages("email.from"),
            to = Seq(request.body.email),
            bodyText = Some(views.txt.emails.resetPassword(user, url).body),
            bodyHtml = Some(views.html.emails.resetPassword(user, url).body)
          ))
          result
        }
      case None => Future.successful(result)
    }

  }
}
