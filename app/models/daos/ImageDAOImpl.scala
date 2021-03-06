package models.daos

import java.sql.Timestamp
import java.util.{ Date, UUID }
import javax.inject.Inject

import models.TaggingImage
import models.tables.ImageTable
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.Json
import play.api.mvc.Results._
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.lifted.TableQuery
import utils.exceptions.HttpError

import scala.concurrent.Future
import scala.concurrent.{ Await, Future }
import play.api.mvc.Results._

class ImageDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends ImageDAO {

  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  val db: JdbcBackend#DatabaseDef = dbConfig.db

  import dbConfig.driver.api._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  override def create(url: String, thumbnailUrl: Option[String], uploadDate: Date, userId: UUID, height: Int, width: Int, filename: String): Future[TaggingImage] = {
    val image = TaggingImage(UUID.randomUUID(), url, thumbnailUrl, Timestamp.from(uploadDate.toInstant), userId, height, width, filename)

    db.run(ImageDAOImpl.images += image).map(_ => image)
  }

  override def listOwnImages(userId: UUID): Future[Seq[TaggingImage]] = {
    val query = ImageDAOImpl.images.filter(_.ownedBy === userId).sortBy(_.uploadedAt.desc).result
    db.run(query)
  }

  override def delete(imageId: UUID, userId: UUID): Future[String] = {
    val query = ImageDAOImpl.images.filter(_.imageId === imageId).map(image => (image.ownedBy === userId, image.url)).result.headOption
    for {
      returnvalue: Option[(Boolean, String)] <- db.run(query)
      url <- returnvalue match {
        case Some((isOwned, url)) => isOwned match {
          case true => {
            db.run(ImageDAOImpl.images.filter(_.imageId === imageId).delete).map(_ => url)
          }
          case false => throw HttpError(play.api.mvc.Results.Forbidden(Json.obj("error" -> "You do not own this image")))
        }
        case None => throw HttpError(NotFound)
      }
    } yield url
  }

  override def setClassificationStart(imageId: UUID, date: Date): Future[Int] = {
    val query = (for { c <- ImageDAOImpl.images if c.imageId === imageId } yield c.classificationStart).update(Some(Timestamp.from(date.toInstant)))
    db.run(query)
  }

  override def setClassificationDuration(imageId: UUID, classificationDuration: Long): Future[Int] = {
    val query = (for { c <- ImageDAOImpl.images if c.imageId === imageId } yield c.classificationDuration).update(Some(classificationDuration))
    db.run(query)
  }

  override def setStatus(imageId: UUID, status: Option[String]): Future[Int] = {
    val query = (for { c <- ImageDAOImpl.images if c.imageId === imageId } yield c.status).update(status)
    db.run(query)
  }

  override def getImage(imageId: UUID): Future[Option[TaggingImage]] = {
    val query = ImageDAOImpl.images.filter(_.imageId === imageId).result.headOption
    db.run(query)
  }
}

object ImageDAOImpl {
  val images = TableQuery[ImageTable]
}
