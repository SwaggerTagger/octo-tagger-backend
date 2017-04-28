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
  override def create(url: String, uploadDate: Date, userId: UUID): Future[TaggingImage] = {
    val image = TaggingImage(UUID.randomUUID(), url, Timestamp.from(uploadDate.toInstant), userId)

    db.run(ImageDAOImpl.images += image).map(_ => image)
  }

  override def listOwnImages(userId: UUID): Future[Seq[TaggingImage]] = {
    val query = ImageDAOImpl.images.filter(_.ownedBy === userId).result
    db.run(query)
  }

  override def delete(imageId: UUID, userId: UUID): Future[Boolean] = {
    val query = ImageDAOImpl.images.filter(_.imageId === imageId).map(_.ownedBy === userId).result.headOption
    for {
      isOwned <- db.run(query)
      _ <- isOwned match {
        case Some(m) => m match {
          case true => db.run(ImageDAOImpl.images.filter(_.imageId === imageId).delete)
          case false => throw HttpError(play.api.mvc.Results.Forbidden("You do not own this image"))
        }
        case None => throw HttpError(NotFound)
      }

    } yield true

  }
}

object ImageDAOImpl {
  val images = TableQuery[ImageTable]
}
