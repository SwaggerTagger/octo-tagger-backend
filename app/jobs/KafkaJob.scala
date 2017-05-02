package jobs

import java.util.UUID

import akka.actor.Actor
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import jobs.KafkaJob.OnStart
import models.Prediction
import models.daos.PredictionDAO
import models.services.Kafka
import play.Logger
import play.api.libs.json.{ JsObject, Json }

import scala.util.{ Failure, Success }

class KafkaJob @Inject() (
  kafka: Kafka,
  predictionDAO: PredictionDAO
) extends Actor {

  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case OnStart =>
      Logger.info("KafkaJob: Starting")

      kafka.source("predictions") match {
        case Failure(e) =>
          Logger.info("Could not connect to Kafka!")
        case Success(source) =>
          source.runForeach(x => {
            Logger.info("Received Prediction from Kafka: " + x.toString)
            predictionDAO.create(kafkaResponseToPredictions(x.value(), x.key()))
          })
      }
  }

  def kafkaResponseToPredictions(input: String, keyID: String): Seq[Prediction] = {
    val json = Json.parse(input)
    (json \ "matches").as[Seq[JsObject]].map(x =>
      Prediction(
        UUID.fromString(keyID),

        (x \ "class").as[String], (x \ "probability").as[Double], (x \ "left").as[Int],
        (x \ "top").as[Int], (x \ "right").as[Int], (x \ "bottom").as[Int]))
  }
}

object KafkaJob {
  case object OnStart
}
