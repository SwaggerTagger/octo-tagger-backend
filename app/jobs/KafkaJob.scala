package jobs

import akka.actor.Actor
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Sink }
import com.google.inject.Inject
import jobs.KafkaJob.OnStart
import models.services.Kafka
import play.Logger

import scala.util.{ Failure, Success }

class KafkaJob @Inject() (
  kafka: Kafka
) extends Actor {

  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case OnStart =>
      Logger.info("KafkaJob: Starting")

      kafka.source("predictions") match {
        case Failure(e) =>
          Logger.info("Could not connect to Kafka!")
        case Success(source) =>
          source.runForeach(x => Logger.info(x.toString))
      }
  }
}

object KafkaJob {
  case object OnStart
}
