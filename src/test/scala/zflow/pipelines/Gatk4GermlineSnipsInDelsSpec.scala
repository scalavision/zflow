package zflow.pipelines

import org.specs2.Spec
import org.specs2.matcher.{DisjunctionMatchers, MatchResultCombinators, ResultMatchers, ValidationMatchers}
//import zflow.tools._
import zflow.syntax._
import zflow.model._
import zflow.Step

import scalaz._

class Gatk4GermlineSnipsInDelsSpec extends Spec with ValidationMatchers with ResultMatchers with MatchResultCombinators with DisjunctionMatchers { def is =
  s2"""Gatk4GermlineSnipsInDelsSpec
    simple printout of the pipeline contents $s1
  """

  val config = Gatk4TermlineConfig(
    workdir = path"/scratch/testdir",
    sampleName = sample"SomeSample",
    ref = Ref(file"/bio/ref"),
    read1 = FastQ(file"/bio/in/read1.fastq"),
    read2 = FastQ(file"/bio/in/read2.fastq"),
    intervalList = file"/bio/filter/gatk_whitelist.interval",
    contamination = "contamination config"
  )

  def s1 = {

    val pipelineSetup = Gatk4GermlineSnipsInDels(config)
    val pipelineBuilder = pipelineSetup.build()

    val pipeline = pipelineBuilder.eval(Step(Vector.empty[Cmd], List.empty[FolderPath]))
    pprint.pprintln(pipeline.zipWithIndex.last._1.cmds.map(_.args.mkString(" ")).mkString("\n"))


    ok
  }
}