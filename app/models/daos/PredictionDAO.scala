package models.daos

import java.util.UUID

import models.Prediction

import scala.concurrent.Future

/**
 * Created by jlzie on 28.04.2017.
 */
trait PredictionDAO {
  def create(predictions: Seq[Prediction]): Future[Seq[Prediction]]
  def getPredictions(images: Seq[UUID]): Future[Seq[Prediction]]
}
