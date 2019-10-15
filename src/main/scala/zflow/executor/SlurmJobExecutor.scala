package zflow.executor

import zflow.model._
import zflow.slurm.SlurmTemplate
import zio._

trait SlurmJobExecutor extends JobExecutor {

  override val jobExecutor = new JobExecutor.Service {
    override def generateSlurmScript(
      job: SlurmJob, 
      sampleName: SampleName, 
      workdir: os.Path,
      config: SlurmConfig
    ): ZIO[Any, Error, SlurmScript] = ZIO.succeed {

      val jobFolder = workdir / s"${job.jobName}"
      val scriptPath = jobFolder / s"${job.jobName}.slurm"
      val slurm = SlurmTemplate.create(
        config, 
        FolderPath(workdir.toString()), 
        job.cmds
      )

      if(!os.exists(jobFolder)) 
        os.makeDir.all(jobFolder)

      os.write.over( scriptPath, slurm)

      job.cmds.collect {

        case b @ SBash(_,_,_, _) => b

      }.foreach { bash =>

        os.write.over(jobFolder / s"${bash.name}.sh", SlurmTemplate.createScript(bash) )

      }

      SlurmScript(scriptPath, jobFolder)

    }

    override def launchSlurmScript(slurm: SlurmScript): ZIO[Any, Error, Unit] = ZIO.succeed {
      val result = os.proc("sbatch", slurm.scriptPath.toString()).call(cwd  = slurm.scriptFolder)
      println(result.toString())
    }

  }

}
