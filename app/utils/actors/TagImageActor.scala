package utils.actors
import java.io.File
import java.util.UUID
import javax.inject.Inject

import akka.actor._
import akka.pattern.pipe
import models.daos.ImageDAO
import utils.azure.BlobStorage

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._
import scala.concurrent._

class TagImageActor @Inject() (
  blobStorage: BlobStorage, imageDAO: ImageDAO) extends Actor {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  import TagImageActor._

  override def receive = {
    case TagImage(file, mimetype, user) =>
      Future {
        val (url, date) = Await.result(blobStorage.upload(file, mimetype), 50.seconds)
        Await.result(imageDAO.create(url, date, user), 10.seconds)
      }(ExecutionContext.Implicits.global) pipeTo sender
  }
}

object TagImageActor {
  def props = Props[TagImageActor]
  case class TagImage(file: File, mimetype: String, user: UUID)
}
