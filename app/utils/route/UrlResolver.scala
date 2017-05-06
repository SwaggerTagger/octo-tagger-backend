package utils.route

/**
 * Created by John on 06.05.2017.
 */
object UrlResolver {
  def toAbsoluteUrl(path: String, scheme: String = "https://")(implicit request: play.api.mvc.Request[Any]) = {
    scheme + request.host + path.replaceFirst("^/?", "/")
  }
}
