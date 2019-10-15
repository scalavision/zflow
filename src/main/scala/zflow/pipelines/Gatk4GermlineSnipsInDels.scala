package zflow.pipelines

import scalaz._
//import Scalaz._

import zflow.PipelineOps
import zflow.tools._
import zflow.model._
import zflow.syntax._

case class Gatk4TermlineConfig(
  workdir: FolderPath,
  sampleName: SampleName,
  ref: Ref,
  read1: FastQ,
  read2: FastQ,
  intervalList: FilePath,
  contamination: String
)

case class Gatk4GermlineSnipsInDels(
  config: Gatk4TermlineConfig
) extends PipelineOps {
  
  import config._

  def mapPhase = {
    val output = bam"$workdir/${sampleName}_mapped.bam"
    step(
      cmd"${bwa.map(ref, read1, read2)} | ${samtools.sortFromStdIn(output)}",
      List(output)
    )
  }

  def haplotypeCaller(bam: Bam) = {
    //TODO: create helper method to simplify this
    val folder = arg"${bam.value.folderPath}"
    val fileName = arg"${bam.value.fileName.dropRight(4)}_haplotyped.vcf"
    //TODO: create GVCF type
    val vcf = vcf"$folder/$fileName"
    val vcfIndex = vcf"$folder/${fileName}.tbi"

    step(gatk4.haplotypeCaller(
      ref, bam, intervalList, vcf, contamination
    ),
      List(vcf, vcfIndex)
    )
  }

  def markDuplicates(in: Bam) = {
    val ff = arg"${in.value.folderPath}"
    val fileName = arg"${in.value.fileName.dropRight(4)}_markduped.bam"
    //TODO: create GVCF type
    val bam = bam"${ff}/$fileName"
    val metrics = file"$ff/markduplicated_statistics.txt"
    step(
      picard.markDuplicates(in, bam, metrics),
      List(bam, metrics)
    )
  }

  import scalaz.State.{ get => log }
  import scala.reflect.{ClassTag}

   def toP[A: ClassTag](v: List[Any]): List[A] = v.collect {
     case a: A => a
   }

   implicit class PathOpsSyntax(val p: List[PathOps]) {
     def toA[A: ClassTag] = toP[A](p)
   }
   
  def build() = 
    for {
      mappedBam <- mapPhase
      _         <- step(samtools.index(mappedBam.out.toA[Bam].head), List.empty[FolderPath])
      _         <- markDuplicates(mappedBam.out.toA[Bam].head)
      _         <- haplotypeCaller(mappedBam.out.toA[Bam].head)
      pipeline  <- log
    } yield pipeline :: Nil

}