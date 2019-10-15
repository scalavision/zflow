package zflow.executor

import zio._
import zflow.model._

trait JobFactory {
  def jobFactory: JobFactory.Service
}

object JobFactory {
  trait Service {
    def createJob(
      cmd: Command, 
      wrapper: Command => Command
    ): ZIO[Any, Nothing, Command]
  }
}