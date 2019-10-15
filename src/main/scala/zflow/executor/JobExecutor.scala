package zflow.executor

import zflow.model._
import zio._

trait JobExecutor {
  
  def jobExecutor: JobExecutor.Service
}

object JobExecutor {
  trait Service {

    def generateSlurmScript(
      job: SlurmJob, 
      sampleName: SampleName, 
      workdir: os.Path,
      config: SlurmConfig
    ): ZIO[Any, Error, SlurmScript]

    def launchSlurmScript(script: SlurmScript): ZIO[Any, Error, Unit]

  }
}
