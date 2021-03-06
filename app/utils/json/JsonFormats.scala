package utils.json

import java.sql.Timestamp
import java.util.UUID

import models.{ Prediction, TaggingImage }
import play.api.libs.json._
import play.api.mvc.{ BodyParser, BodyParsers }

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
  implicit object PredictionFormat extends Writes[Prediction] {
    override def writes(o: Prediction): JsValue = Json.format[Prediction].writes(o) - ("imageId")
  }
  def writeImageswithPredicitions(images: Seq[TaggingImage], predicitions: Seq[Prediction]): JsValue = {

    val predictionMap = scala.collection.mutable.HashMap.empty[UUID, scala.collection.mutable.MutableList[Prediction]]
    predicitions.foreach(prediction => {
      predictionMap.get(prediction.imageId) match {
        case Some(list: scala.collection.mutable.MutableList[Prediction]) => list.+=(prediction)
        case _ => predictionMap += ((prediction.imageId, scala.collection.mutable.MutableList(prediction)))
      }
    })
    val imagesJson = images.map(image => {
      val jImage = Json.toJson(image).as[JsObject]
      predictionMap.get(image.imageId) match {
        case Some(preds) => jImage.+(("predictions", Json.toJson(preds)))
        case _ => jImage
      }

    }).seq
    Json.toJson(imagesJson)
  }
  def writeImageswithPredictions(image: TaggingImage, predictions: Seq[Prediction]): JsValue = {
    val jImage = Json.toJson(image).as[JsObject]
    predictions.length match {
      case l if l > 0 => jImage.+(("predictions", Json.toJson(predictions)))
      case _ => jImage
    }
  }
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  def validateJson[A: Reads]: BodyParser[A] = BodyParsers.parse.json.validate(
    _.validate[A].asEither.left.map(e => play.api.mvc.Results.BadRequest(JsError.toJson(e)))
  )
}
