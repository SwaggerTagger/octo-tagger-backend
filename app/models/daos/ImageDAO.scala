package models.daos

import java.util.{ Date, UUID }

import models.TaggingImage

import scala.concurrent.Future

/**
 * Created by jlzie on 26.04.2017.
 */
trait ImageDAO {
  def create(url: String, thumbnailUrl: Option[String], uploadDate: Date, userId: UUID, height: Int, width: Int, filename: String): Future[TaggingImage]
  def listOwnImages(userId: UUID): Future[Seq[TaggingImage]]
  def getImage(imageId: UUID): Future[Option[TaggingImage]]
  def delete(imageId: UUID, userId: UUID): Future[String]
  def setClassificationStart(imageId: UUID, date: Date): Future[Int]
  def setClassificationDuration(imageId: UUID, classificationDuration: Long): Future[Int]
  def setStatus(imageId: UUID, status: Option[String]): Future[Int]
}
