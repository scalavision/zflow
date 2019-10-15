package zflow.slurm

import zflow.model._

object SlurmTemplate {

  def createScript(b: SBash): String = {
    s"""|#!/bin/bash
  |set -euo pipefail
  |${b.script}
  |""".stripMargin
  }

  def createScriptRunner(
    cmds: List[Command],
  ): String = {
    cmds
      .map {
        case Cmd(v) => v.mkString(" ")
        case Bash(_,_,_) => ""
        case SBash(_, name, startScript, _) =>
          s"""|echo "Launching script"
      |chmod +x $$SCRATCH/workdir/${name}.sh
      |$startScript
      |""".stripMargin
      }
      .mkString("\n")
  }

  def prepareScript(
    cmds: List[Command], 
    scriptFolder: FolderPath
  ): String = {
    cmds
      .map {
        case Cmd(_) => ""
        case Bash(_,_,_) => ""
        case SBash(_, name, _, _) =>
          s"cp ${scriptFolder.value}/${name}.sh $$SCRATCH/workdir"
      }
      .filter(_.nonEmpty)
      .mkString("\n")
  }

  def create(
      config: SlurmConfig,
      scriptDir: FolderPath,
      cmds: List[Command]
  ) =
    s"""|#!/bin/bash
    |# Job name:
    |#SBATCH --job-name=${config.name}-${new java.util.Date().getTime()}
    |#
    |# Project:
    |#SBATCH --account=${config.account}
    |#
    |# Wall clock limit:
    |#SBATCH --time=${config.timeout}
    |#
    |#SBATCH --cpus-per-task=${config.memPrCpu}
    |#
    |# Max memory usage:
    |#SBATCH --mem-per-cpu=${config.memPrCpu}
    |
    |## Set up job environment:
    |module purge   # clear any inherited modules
    |set -o errexit # exit on errors
    |
    |module load singularity/2.6.1
    |
    |## Copy input files to the work directory:
    |mkdir -p $$SCRATCH/bam
    |mkdir -p $$SCRATCH/ref
    |mkdir -p $$SCRATCH/workdir
    |${prepareScript(cmds, scriptDir)}
    |
    |## Make sure the results are copied back to the submit directory (see Work Directory below):
    |chkfile $$SCRATCH/workdir
    |
    |## Do some work:
    |cd $$SCRATCH/workdir
    |
    |${createScriptRunner(cmds)}
    """.stripMargin

}
