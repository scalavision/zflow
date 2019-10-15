package zflow.executor

import zio._
import zflow.model._

object JobExecutorService {
  def generateSlurmScript(
    job: SlurmJob, 
    sampleName: SampleName, 
    workdir: os.Path,
    config: SlurmConfig
    ): ZIO[JobExecutor, Error, SlurmScript] = 
      ZIO.accessM[JobExecutor](_.jobExecutor.generateSlurmScript(
        job: SlurmJob, 
        sampleName: SampleName, 
        workdir: os.Path,
        config: SlurmConfig
      ))
}
