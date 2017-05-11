package models.daos
import java.sql.{ Date, Timestamp }
import java.util.UUID
import javax.inject.Inject

import models.SSEToken
import models.tables.SSETokenTable
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.lifted.TableQuery
import utils.Helper

import scala.concurrent.Future
import scala.concurrent.duration._
/**
 * Created by John on 11.05.2017.
 */
class SSETokenImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends SSETokenDAO {
  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]
  val db: JdbcBackend#DatabaseDef = dbConfig.db

  import dbConfig.driver.api._
  import play.api.libs.concurrent.Execution.Implicits.defaultContext
  val now = SimpleFunction.nullary[Timestamp]("NOW")
  override def getToken(token: String): Future[Option[SSEToken]] = {

    val query = SSETokenImpl.tokens.filter(tok => tok.token === token && tok.expiry < now).result.headOption
    db.run(query)
  }

  override def createToken(userId: UUID): Future[SSEToken] = {
    val token = SSEToken(Helper.randomAlphanumericString(40), userId, new Timestamp(System.currentTimeMillis() + 1.minute.toMillis))
    db.run(SSETokenImpl.tokens += token).map(_ => token)
  }

  override def cleanToken(): Future[Int] = {
    val query = SSETokenImpl.tokens.filter(_.expiry < now).delete
    db.run(query)
  }
}

object SSETokenImpl {
  val tokens = TableQuery[SSETokenTable]
}
