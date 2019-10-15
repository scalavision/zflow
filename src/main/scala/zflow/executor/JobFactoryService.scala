package zflow.executor

import zio._
import zflow.model._

object JobFactoryService {

  def createJob(cmd: Command, wrapper: Command => Command): ZIO[JobFactory, Nothing, Command] = 
    ZIO.accessM(_.jobFactory.createJob(cmd, wrapper))

}