package models.tables

import java.sql.Timestamp
import java.util.{ Date, UUID }

import models.TaggingImage
import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape

class ImageTable(tag: Tag) extends Table[TaggingImage](tag, "images") {
  def imageId: Rep[UUID] = column[UUID]("image_id", O.PrimaryKey)

  def url: Rep[String] = column[String]("url")

  def thumbnailUrl: Rep[Option[String]] = column[Option[String]]("thumbnail_url")

  def uploadedAt: Rep[Timestamp] = column[Timestamp]("uploaded_at")

  def ownedBy: Rep[UUID] = column[UUID]("owned_by")

  def classificationStart: Rep[Option[Timestamp]] = column[Option[Timestamp]]("classification_start")

  def classificationDuration: Rep[Option[Long]] = column[Option[Long]]("classification_duration")

  def height: Rep[Int] = column[Int]("height")

  def width: Rep[Int] = column[Int]("width")

  def filename: Rep[String] = column[String]("filename")

  def status: Rep[Option[String]] = column[Option[String]]("status")
  override def * : ProvenShape[TaggingImage] = (imageId, url, thumbnailUrl, uploadedAt, ownedBy, height, width, filename, classificationStart, classificationDuration, status) <> (TaggingImage.tupled, TaggingImage.unapply)
}
