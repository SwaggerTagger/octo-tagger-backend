package modules

import jobs.{ AuthTokenCleaner, KafkaJob, SSEDebug, Scheduler }
import models.services.{ Kafka, KafkaImpl }
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import utils.actors.{ KafkaWriteActor, SSEPublisher, TagImageActor }

/**
 * The job module.
 */
class JobModule extends ScalaModule with AkkaGuiceSupport {

  /**
   * Configures the module.
   */
  def configure() = {
    bindActor[KafkaJob]("kafka-job")
    bindActor[KafkaWriteActor]("kafka-write-actor")
    bindActor[TagImageActor]("tag-image-actor")
    bindActor[SSEDebug]("sse-debug-job")
    bindActor[SSEPublisher]("sse-publisher")
    bind(classOf[Kafka]).to(classOf[KafkaImpl])
    bind[Scheduler].asEagerSingleton()
  }
}
