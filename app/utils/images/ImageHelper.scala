package utils.images

import java.io.File
import javax.imageio.ImageIO

import com.google.inject.Inject
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import play.api.libs.Files.TemporaryFile

import scala.concurrent.Future

/**
 * Created by John on 03.05.2017.
 */
class ImageHelper @Inject() (configuration: play.api.Configuration) {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  def getImageDimensions(imageFile: File): Future[(Int, Int)] = Future[(Int, Int)] {
    val image = ImageIO.read(imageFile)
    (image.getHeight, image.getWidth)
  }
  lazy val maxWidth = configuration.underlying.getInt("octotagger.images.thumbnail.maxWidth")
  lazy val maxHeight = configuration.underlying.getInt("octotagger.images.thumbnail.maxHeight")
  implicit val writer = JpegWriter().withCompression(50).withProgressive(true)
  def createThumbnail(image: Image): Future[TemporaryFile] = Future[TemporaryFile] {
    val file = File.createTempFile("thumbnails", ".jpg")
    image.max(maxWidth, maxHeight).output(file)
    new TemporaryFile(file)
  }

}
