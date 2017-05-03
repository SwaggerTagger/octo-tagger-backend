package models.tables

import java.sql.Timestamp
import java.util.{ Date, UUID }

import models.{ Prediction, TaggingImage }
import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape

/**
 * Created by jlzie on 28.04.2017.
 */
class PredictionTable(tag: Tag) extends Table[Prediction](tag, "predictions") {
  def imageId: Rep[UUID] = column[UUID]("image_id", O.PrimaryKey)

  def predictionId: Rep[Int] = column[Int]("prediction_id", O.PrimaryKey, O.AutoInc)

  def category: Rep[String] = column[String]("category")
  def probability: Rep[Double] = column[Double]("probability")
  def left: Rep[Int] = column[Int]("leftx")
  def top: Rep[Int] = column[Int]("topy")
  def right: Rep[Int] = column[Int]("rightx")
  def bottom: Rep[Int] = column[Int]("bottomy")

  override def * : ProvenShape[Prediction] = (imageId, category, probability, left, top, right, bottom, predictionId) <> (Prediction.tupled, Prediction.unapply)
}
