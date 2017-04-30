package utils.actors
import java.io.File
import java.util.UUID
import javax.inject.Inject

import akka.actor._
import models.daos.ImageDAO
import utils.azure.BlobStorage

class TagImageActor @Inject() (
  blobStorage: BlobStorage, imageDAO: ImageDAO) extends Actor {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  import TagImageActor._

  override def receive = {
    case TagImage(file, mimetype, user) =>
      for {
        (url, date) <- blobStorage.upload(file, mimetype)
        image <- imageDAO.create(url, date, user)
      } yield image;

  }
}

object TagImageActor {
  def props = Props[TagImageActor]
  case class TagImage(file: File, mimetype: String, user: UUID)
}
