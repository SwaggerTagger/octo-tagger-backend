package jobs

import java.util.{ Date, UUID }

import akka.actor.Actor
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import jobs.KafkaJob.OnStart
import models.Prediction
import models.daos.{ ImageDAO, PredictionDAO }
import models.services.Kafka
import play.Logger
import play.api.libs.json.{ JsObject, JsValue, Json }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

class KafkaJob @Inject() (
  kafka: Kafka,
  imageDAO: ImageDAO,
  predictionDAO: PredictionDAO
) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case OnStart =>
      Logger.info("KafkaJob: Starting")

      kafka.source("classification-status") match {
        case Failure(e) =>
          Logger.info(s"Could not connect to Kafka! Error: $e")
        case Success(source) =>
          source.runForeach(x => {
            Logger.info(s"Received Status Update from Kafka: ${x.toString}")
            val json = Json.parse(x.value())

            (json \ "status").asOpt[String] match {
              case Some("CLASSIFICATION_STARTING") =>
                for {
                  status <- imageDAO.setClassificationStart(UUID.fromString(x.key()), new Date)
                } yield sender ! status

              case Some(other) =>
                for {
                  status <- imageDAO.setStatus(UUID.fromString(x.key()), Some(other))
                } yield sender ! status

              case None =>
                Logger.warn(s"Invalid Message from Kafka: ${x.value()}")
            }
          })
      }

      kafka.source("predictions") match {
        case Failure(e) =>
          Logger.info(s"Could not connect to Kafka! Error: $e")
        case Success(source) =>
          source.runForeach(x => {
            Logger.info("Received Prediction from Kafka: " + x.toString)
            val json = Json.parse(x.value())

            val preds = kafkaResponseToPredictions(json, x.key())
            val duration = getClassificationDuration(json)

            Logger.debug(s"Setting Classification duration of $duration for id=${x.key()}")
            Logger.debug(s"Writing ${preds.seq.size} Predictions into Database (id=${x.key()})")
            for {
              _ <- imageDAO.setClassificationDuration(UUID.fromString(x.key()), duration).recoverWith({
                case e =>
                  Logger.debug(s"Failed writing classificationduration into Database: ${e.getMessage}")
                  Future.failed(e)
              })
              _ <- predictionDAO.create(preds)
                .recoverWith {
                  case e =>
                    Logger.error(s"Failed writing into Database: ${e.getMessage}")
                    Future.failed(e)
                }
            } yield sender() ! true
          })
      }
  }

  def getClassificationDuration(json: JsValue): Long = {
    ((json \ "time").as[Double] * 1000).floor.toLong
  }

  def kafkaResponseToPredictions(json: JsValue, keyID: String): Seq[Prediction] = {
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
