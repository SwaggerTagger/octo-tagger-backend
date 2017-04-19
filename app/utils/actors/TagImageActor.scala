package utils.actors
import java.io.File

import akka.actor._
/**
  * Created by jlzie on 19.04.2017.
  */
class ImageUploadActor  extends Actor{

  import ImageUploadActor._

  override def receive = {
    case UploadedImage(file,mimetype) =>

  }
}

object ImageUploadActor {
  def props = Props[ImageUploadActor]
  case class UploadedImage(file:File,mimetype: String)
}
