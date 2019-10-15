package zflow.executor

import zio._
import zflow.model._

trait SingularityJobFactory extends JobFactory {
 
  override val jobFactory = new JobFactory.Service {
    def createJob(cmd: Command, wrapper: Command => Command): ZIO[Any, Nothing, Command] =
      ZIO.succeed(wrapper(cmd))
  }

}

object SingularityJobFactory extends SingularityJobFactory