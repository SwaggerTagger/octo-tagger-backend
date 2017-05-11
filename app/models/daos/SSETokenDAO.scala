package models.daos

import java.util.UUID

import models.SSEToken

import scala.concurrent.Future

/**
 * Created by John on 11.05.2017.
 */
trait SSETokenDAO {
  def getToken(token: String): Future[Option[SSEToken]]
  def createToken(userId: UUID): Future[SSEToken]
  def cleanToken(): Future[Int]
}
