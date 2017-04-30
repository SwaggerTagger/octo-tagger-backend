package modules

import jobs.{ AuthTokenCleaner, KafkaJob, Scheduler }
import models.services.{ Kafka, KafkaImpl }
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
 * The job module.
 */
class JobModule extends ScalaModule with AkkaGuiceSupport {

  /**
   * Configures the module.
   */
  def configure() = {
    bindActor[KafkaJob]("kafka-job")
    bind(classOf[Kafka]).to(classOf[KafkaImpl])
    bind[Scheduler].asEagerSingleton()
  }
}
