package zflow.tools

import zflow.syntax._
import zflow.model._

object gatk4 {

  val gatk = arg"gatk"

  def haplotypeCaller(
    ref: Ref,
    in: Bam,
    intervalList: FilePath,
    out: Vcf,
    contamination: String
  ) = {
    val contamin = arg"$contamination"
    cmd"$gatk HaplotypeCaller -R $ref -I $in -L $intervalList -O $out -contamination $contamin"
  }
  

}