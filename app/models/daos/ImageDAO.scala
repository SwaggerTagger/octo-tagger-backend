package models.daos

import java.util.{ Date, UUID }

import models.TaggingImage

import scala.concurrent.Future

/**
 * Created by jlzie on 26.04.2017.
 */
trait ImageDAO {
  def create(url: String, uploadDate: Date, userId: UUID): Future[TaggingImage]
  def listOwnImages(userId: UUID): Future[Seq[TaggingImage]]
  def delete(imageId: UUID, userId: UUID): Future[String]
  def setClassificationStart(imageId: UUID, date: Date): Future[Int]
  def setClassificationDuration(imageId: UUID, classificationDuration: Long): Future[Int]
}
