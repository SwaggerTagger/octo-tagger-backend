package models.formatters

import java.sql.Timestamp

import models.TaggingImage
import play.api.libs.json._

/**
 * Created by jlzie on 28.04.2017.
 */
object JsonFormats {
  implicit object timestampFormat extends Format[Timestamp] {
    def reads(json: JsValue) = {

      JsSuccess(new Timestamp(json.as[Long]))
    }
    def writes(ts: Timestamp) = {
      JsNumber(ts.getTime)
    }
  }
  implicit val taggingImageFormat = Json.format[TaggingImage]

}
