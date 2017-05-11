package models.tables

import java.sql.Timestamp
import java.util.UUID

import models.{ Prediction, SSEToken }
import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape
/**
 * Created by John on 11.05.2017.
 */

class SSETokenTable(tag: Tag) extends Table[SSEToken](tag, "predictions") {
  def token: Rep[String] = column[String]("token", O.PrimaryKey)
  def userId: Rep[UUID] = column[UUID]("image_id", O.PrimaryKey)
  def expiry: Rep[Timestamp] = column[Timestamp]("expiry")
  override def * : ProvenShape[SSEToken] = (token, userId, expiry) <> (SSEToken.tupled, SSEToken.unapply)
}

