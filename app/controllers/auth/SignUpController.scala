package controllers.auth

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers._
import controllers.{ WebJarAssets, auth }
import models.User
import models.services.{ AuthTokenService, UserService }
import play.api.data
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.{ Action, AnyContent, Controller, Request }
import utils.auth.DefaultEnv
import utils.json.JsonFormats
import utils.route.UrlResolver

import scala.concurrent.Future

/**
 * The `Sign Up` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param authTokenService       The auth token service implementation.
 * @param avatarService          The avatar service implementation.
 * @param passwordHasherRegistry The password hasher registry.
 * @param mailerClient           The mailer client.
 * @param webJarAssets           The webjar assets implementation.
 */
class SignUpController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
  passwordHasherRegistry: PasswordHasherRegistry,
  mailerClient: MailerClient,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {
  case class SignUpInformation(email: String, firstName: Option[String], lastName: Option[String], password: String)
  implicit val SignuPRead = Json.format[SignUpInformation]
  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async(JsonFormats.validateJson[SignUpInformation]) { implicit request =>
    val SignUpInformation(email, firstName, lastName, password) = request.body
    val result = Ok(Json.obj("info" -> Messages("sign.up.email.sent", email)))
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
    userService.retrieve(loginInfo).flatMap {
      case Some(user) =>
        val url = UrlResolver.toAbsoluteUrl("/#/login")
        mailerClient.send(Email(
          subject = Messages("email.already.signed.up.subject"),
          from = Messages("email.from"),
          to = Seq(email),
          bodyText = Some(views.txt.emails.alreadySignedUp(user, url).body),
          bodyHtml = Some(views.html.emails.alreadySignedUp(user, url).body)
        ))

        Future.successful(result)
      case None =>
        val authInfo = passwordHasherRegistry.current.hash(password)
        val user = User(
          userID = UUID.randomUUID(),
          loginInfo = loginInfo,
          firstName = firstName,
          lastName = lastName,
          fullName = Some(firstName + " " + lastName),
          email = Some(email),
          avatarURL = None,
          activated = false
        )
        for {
          avatar <- avatarService.retrieveURL(email)
          user <- userService.save(user.copy(avatarURL = avatar))
          authInfo <- authInfoRepository.add(loginInfo, authInfo)
          authToken <- authTokenService.create(user.userID)
        } yield {
          val url = auth.routes.ActivateAccountController.activate(authToken.id).absoluteURL()(request)
          mailerClient.send(Email(
            subject = Messages("email.sign.up.subject"),
            from = Messages("email.from"),
            to = Seq(email),
            bodyText = Some(views.txt.emails.signUp(user, url).body),
            bodyHtml = Some(views.html.emails.signUp(user, url).body)
          ))

          silhouette.env.eventBus.publish(SignUpEvent(user, request))
          result
        }
    }

  }
}
