package utils

/**
 * Created by John on 11.05.2017.
 */
object Helper {
  // Random generator
  val random = new scala.util.Random(new java.security.SecureRandom())

  // Generate a random string of length n from the given alphabet
  def randomString(alphabet: String)(n: Int): String =
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString

  // Generate a random alphabnumeric string of length n
  def randomAlphanumericString(n: Int) =
    randomString("abcdefghijklmnopqrstuvwxyz0123456789")(n)
}
