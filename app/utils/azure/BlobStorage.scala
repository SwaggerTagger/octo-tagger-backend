package utils.azure

import java.io.{ File, FileInputStream }

import com.google.inject.Inject

import scala.concurrent.Future

/**
 * Created by jlzie on 14.04.2017.
 */
class BlobStorage @Inject() (configuration: play.api.Configuration) {
  import scala.concurrent.ExecutionContext.Implicits.global
  // Random generator
  val random = new scala.util.Random(new java.security.SecureRandom())

  // Generate a random string of length n from the given alphabet
  def randomString(alphabet: String)(n: Int): String =
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString

  // Generate a random alphabnumeric string of length n
  def randomAlphanumericString(n: Int) =
    randomString("abcdefghijklmnopqrstuvwxyz0123456789")(n)
  // Define the connection-string with your values

  def upload(file: File, mimeType: String): Future[String] = scala.concurrent.Future[String] {
    import com.microsoft.azure.storage.CloudStorageAccount
    try { // Retrieve storage account from connection-string.
      val storageAccount = CloudStorageAccount.parse(configuration.underlying.getString("octotagger.azure.pictureblob.connection.string"))
      // Create the blob client.
      val blobClient = storageAccount.createCloudBlobClient
      // Retrieve reference to a previously created container.
      val container = blobClient.getContainerReference("pictures")
      val name = randomAlphanumericString(40)
      val extension = mimeType match {
        case "image/jpeg" => ".jpg"
        case "image/png" => ".png"
      }
      val blob = container.getBlockBlobReference(name + extension)
      blob.upload(new FileInputStream(file), file.length())
      blob.getProperties().setContentType(mimeType)
      blob.uploadProperties()
      blob.getUri.toString
    } catch {
      case e: Exception =>
        // Output the stack trace.
        e.printStackTrace()
        throw e
    }
  }
}
