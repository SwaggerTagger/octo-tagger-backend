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

import scala.concurrent.Future

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

    db.run(ImageDAOImpl.images += image).map(result => image)
  }

}

object ImageDAOImpl {
  val images = TableQuery[ImageTable]
}
