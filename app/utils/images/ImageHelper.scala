package utils.images

import java.io.File
import javax.imageio.ImageIO

import scala.concurrent.Future

/**
 * Created by John on 03.05.2017.
 */
object ImageHelper {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  def getImageDimensions(imageFile: File): Future[(Int, Int)] = Future[(Int, Int)] {
    val image = ImageIO.read(imageFile)
    (image.getHeight, image.getWidth)
  }
}
