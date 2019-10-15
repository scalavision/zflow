package zflow.executor

import zflow.model._
import zio._

trait SlurmJobExecutor extends JobExecutor {

  override val jobExecutor = new JobExecutor.Service {
    override def generateSlurmScript(
      job: Job, 
      sampleName: SampleName, 
      workdir: os.Path
    ): ZIO[Any, Error, SlurmScript] = ZIO.succeed {

      val jobFolder = workdir / s"${job.jobName}"
      val scriptPath = jobFolder / s"${job.jobName}.slurm"
      val slurm = createSlurmTemplate(job, job.timeout)

      if(!os.exists(jobFolder)) 
        os.makeDir.all(jobFolder)

      os.write.over( scriptPath, slurm)
      
      println("writing scripts to disk:")
      pprint.pprintln(job)

      job.cmds.collect {

        case b @ SBash(_,_,_, _) => b

      }.foreach { bash =>

        os.write.over(jobFolder / s"${bash.name}.sh", createScript(bash) )

      }

      SlurmScript(scriptPath, jobFolder)

    }

    override def launchSlurmScript(slurm: SlurmScript): ZIO[Any, Error, Unit] = ZIO.succeed {
      val result = os.proc("sbatch", slurm.scriptPath.toString()).call(cwd  = slurm.scriptFolder)
      println(result.toString())
    }

  }

}
