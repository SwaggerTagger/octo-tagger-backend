package utils.actors
import java.io.File
import java.util.UUID
import javax.inject.Inject

import akka.actor._
import com.sksamuel.scrimage.Image
import models.daos.ImageDAO
import utils.azure.BlobStorage
import utils.images.ImageHelper

import scala.concurrent._

class TagImageActor @Inject() (
  blobStorage: BlobStorage, imageDAO: ImageDAO, imageHelper: ImageHelper) extends Actor {
  import TagImageActor._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext

  override def receive = {
    case TagImage(file, user, filename) =>
      val sen = sender
      try {
        val image = Image.fromFile(file)
        for {
          (height, width) <- Future.successful((image.height, image.width))
          thumbnail <- imageHelper.createThumbnail(image)
          convertedImage <- imageHelper.convertImageToJpeg(image)
          (url, date) <- blobStorage.upload(convertedImage.file, "image/jpeg")
          (thumbnailUrl, _) <- blobStorage.upload(thumbnail.file, "image/jpeg")
          image <- imageDAO.create(url, thumbnailUrl, date, user, height, width, filename)
        } yield sen ! Right(image)
      } catch {
        case e: Exception => {
          sen ! Left(e)
        }
      }
  }
}

object TagImageActor {
  def props = Props[TagImageActor]
  case class TagImage(file: File, user: UUID, filename: String)
}
