package models.daos

import java.sql.Timestamp
import java.util.{ Date, UUID }
import javax.inject.Inject

import models.TaggingImage
import models.tables.ImageTable
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.lifted.TableQuery
import utils.exceptions.HttpError

import scala.collection.LinearSeq
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import play.api.mvc.Results._
/**
 * Created by jlzie on 26.04.2017.
 */
class ImageDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends ImageDAO {

  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  val db: JdbcBackend#DatabaseDef = dbConfig.db

  import dbConfig.driver.api._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  override def create(url: String, uploadDate: Date, userId: UUID, height: Int, width: Int): Future[TaggingImage] = {
    val image = TaggingImage(UUID.randomUUID(), url, Timestamp.from(uploadDate.toInstant), userId, height, width)

    db.run(ImageDAOImpl.images += image).map(_ => image)
  }

  override def listOwnImages(userId: UUID): Future[Seq[TaggingImage]] = {
    val query = ImageDAOImpl.images.filter(_.ownedBy === userId).result
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
          case false => throw HttpError(play.api.mvc.Results.Forbidden("You do not own this image"))
        }
        case None => throw HttpError(NotFound)
      }
    } yield url
  }
}

object ImageDAOImpl {
  val images = TableQuery[ImageTable]
}
