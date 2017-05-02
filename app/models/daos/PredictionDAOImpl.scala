package models.daos

import java.sql.Timestamp
import java.util.{ Date, UUID }
import javax.inject.Inject

import models.{ Prediction, TaggingImage }
import models.tables.PredictionTable
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
 * Created by jlzie on 28.04.2017.
 */
class PredictionDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends PredictionDAO {

  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  val db: JdbcBackend#DatabaseDef = dbConfig.db

  import dbConfig.driver.api._
  import PredictionDAOImpl._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  override def create(predictions: Seq[Prediction]): Future[Seq[Prediction]] = {
    val query = predictionsTable.returning(predictionsTable.map(_.predictionId)).into((prediction, id) => prediction.copy(predictionId = id)) ++= predictions
    db.run(query).map(_.seq)
  }

  override def getPredictions(images: Seq[UUID]): Future[Seq[Prediction]] = {
    val query = predictionsTable.filter(_.imageId inSet images).result
    db.run(query)
  }
}

object PredictionDAOImpl {
  val predictionsTable = TableQuery[PredictionTable]
}
