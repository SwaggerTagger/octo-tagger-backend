package jobs

import akka.actor.{ ActorRef, ActorSystem }
import com.google.inject.Inject
import com.google.inject.name.Named
import scala.concurrent.duration._

/**
 * Schedules the jobs.
 */
class Scheduler @Inject() (
  system: ActorSystem,
  @Named("kafka-job") kafkaJob: ActorRef,
  @Named("sse-debug-job") ssedebug: ActorRef) {
  import scala.concurrent.ExecutionContext.Implicits.global
  //system.scheduler.schedule(5.seconds, 5.seconds, ssedebug, SSEDebug.OnStart)
  system.scheduler.scheduleOnce(50.milliseconds) {
    kafkaJob ! KafkaJob.OnStart
  }
}
