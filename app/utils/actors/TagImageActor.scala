package utils.actors
import java.io.File

import akka.actor._
/**
  * Created by jlzie on 19.04.2017.
  */
class TagImageActor  extends Actor{

  import TagImageActor._

  override def receive = {
    case TagImage(file,mimetype) =>

  }
}

object TagImageActor {
  def props = Props[TagImageActor]
  case class TagImage(file:File, mimetype: String)
}
