package models.tables

import java.sql.Timestamp
import java.util.{ Date, UUID }

import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape

case class TaggingImage(url: String, uploadedAt: Timestamp, ownedBy: UUID, classifiedAt: Option[Timestamp] = None, classificationDuration: Option[Long] = None)

class ImageTable(tag: Tag) extends Table[TaggingImage](tag, "images") {

  def url: Rep[String] = column[String]("id", O.PrimaryKey)

  def uploadedAt: Rep[Timestamp] = column[Timestamp]("uploaded_at")

  def ownedBy: Rep[UUID] = column[UUID]("owned_by")

  def classifiedAt: Rep[Option[Timestamp]] = column[Option[Timestamp]]("classified_at")

  def classificationDuration: Rep[Option[Long]] = column[Option[Long]]("classification_duration")

  override def * : ProvenShape[TaggingImage] = (url, uploadedAt, ownedBy, classifiedAt, classificationDuration) <> (TaggingImage.tupled, TaggingImage.unapply)
}
