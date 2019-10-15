package zflow.executor

import zflow.model._
import zflow.slurm.SlurmTemplate
import zio._

trait DebugJobExecutor extends JobExecutor {
 
  override val jobExecutor = new JobExecutor.Service {
    override def generateSlurmScript(
      job: SlurmJob, 
      sampleName: SampleName, 
      workdir: os.Path,
      config: SlurmConfig
    ):ZIO[Any, Error, SlurmScript] = ZIO.succeed {

      val jobFolder = workdir / s"${sampleName}" / s"${job.jobName}"
      val jobPath = jobFolder / s"${job.jobName}.slurm"
      val slurm = SlurmTemplate.create(
        config, 
        FolderPath(workdir.toString()), 
        job.cmds
      )
      println("script that would be written to disk\n" + slurm)
      SlurmScript(jobPath, jobFolder)
    }

    override def launchSlurmScript(slurm: SlurmScript): ZIO[Any, Error, Unit] = ZIO.succeed {
      println(s"sbatch ${slurm.scriptPath}")
    }

  }

}
