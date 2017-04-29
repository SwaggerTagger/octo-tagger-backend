package modules

import com.google.inject.AbstractModule
import models.daos._
import models.services.{ AuthTokenService, AuthTokenServiceImpl }
import net.codingwell.scalaguice.ScalaModule
import play.api.ApplicationLoader

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure(): Unit = {
    ApplicationLoader
    bind[ImageDAO].to[ImageDAOImpl]
    bind[PredictionDAO].to[PredictionDAOImpl]
    bind[AuthTokenDAO].to[AuthTokenDAOImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
  }
}
