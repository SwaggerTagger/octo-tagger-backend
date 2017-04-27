package models.daos

import java.util.{ Date, UUID }

import models.TaggingImage

import scala.concurrent.Future

/**
 * Created by jlzie on 26.04.2017.
 */
trait ImageDAO {
  def create(url: String, uploadDate: Date, userId: UUID): Future[TaggingImage]
}
