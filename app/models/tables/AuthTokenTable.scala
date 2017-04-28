package models.tables

import java.sql.Timestamp
import java.util.UUID

import models.AuthToken
import slick.driver.PostgresDriver.api._
import slick.lifted.ProvenShape

class AuthTokenTable(tag: Tag) extends Table[AuthToken](tag, "auth_tokens") {

  def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)

  def userID: Rep[UUID] = column[UUID]("user_id")

  def expiry: Rep[Timestamp] = column[Timestamp]("expiry")

  override def * : ProvenShape[AuthToken] = (id, userID, expiry) <> (AuthToken.tupled, AuthToken.unapply)

}
