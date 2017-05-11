package utils

import akka.util.ByteString
import play.api.libs.json.Json
import utils.json.JsonFormats

/**
 * Created by John on 11.05.2017.
 */
object Helper {
  // Random generator
  val random = new scala.util.Random(new java.security.SecureRandom())

  // Generate a random string of length n from the given alphabet
  def randomString(alphabet: String)(n: Int): String =
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString

  // Generate a random alphabnumeric string of length n
  def randomAlphanumericString(n: Int) =
    randomString("abcdefghijklmnopqrstuvwxyz0123456789")(n)
  case class SSEvent(id: Option[String], eventType: Option[String], data: Option[String])
  def convertEventToByteString(event: SSEvent) = {
    (event.id match {
      case Some(id) => ByteString(s"id: $id}\n")
      case _ => ByteString()
    }) ++
      (event.eventType match {
        case Some(eType) => ByteString(s"event: ${eType}\n")
        case _ => ByteString()
      }) ++
      (event.data match {
        case Some(data) => ByteString(s"data: $data\n")
        case _ => ByteString()
      }) ++
      ByteString("\n")
  }
}
