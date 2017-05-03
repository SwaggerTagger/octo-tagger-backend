package utils.json

import java.sql.Timestamp
import java.util.UUID

import models.{ Prediction, TaggingImage }
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

  def writeImageswithPredicitions(images: Seq[TaggingImage], predicitions: Seq[Prediction]) = {
    implicit object PredictionFormat extends Writes[Prediction] {
      override def writes(o: Prediction): JsValue = Json.format[Prediction].writes(o) - ("imageId")
    }

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
}
