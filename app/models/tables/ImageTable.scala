package models.tables

import java.sql.Timestamp
import java.util.{ Date, UUID }

import models.TaggingImage
import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape

class ImageTable(tag: Tag) extends Table[TaggingImage](tag, "images") {
  def imageId: Rep[UUID] = column[UUID]("image_id", O.PrimaryKey)

  def url: Rep[String] = column[String]("url")

  def uploadedAt: Rep[Timestamp] = column[Timestamp]("uploaded_at")

  def ownedBy: Rep[UUID] = column[UUID]("owned_by")

  def classificationStart: Rep[Option[Timestamp]] = column[Option[Timestamp]]("classification_start")

  def classificationDuration: Rep[Option[Long]] = column[Option[Long]]("classification_duration")

  def height: Rep[Int] = column[Int]("height")

  def width: Rep[Int] = column[Int]("width")

  override def * : ProvenShape[TaggingImage] = (imageId, url, uploadedAt, ownedBy, height, width, classificationStart, classificationDuration) <> (TaggingImage.tupled, TaggingImage.unapply)
}
