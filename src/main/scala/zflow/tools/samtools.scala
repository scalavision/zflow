package zflow.tools

import zflow.syntax._
import zflow.model._

object samtools {

  val samtools = arg"samtools"
  
  def convertBamToCram(
    ref: Ref,
    in: Cram,
    out: Bam
  ) = 
    cmd"$samtools view -h -T $ref $in | $samtools view -b -o $out -"

  def index(
    bam: Bam,
  ) = cmd"$samtools index -b $bam"

  def pipeToSort(
    out: Bam
  ) = cmd"$samtools sort -o $out -"
  
  // Streams from StdIn, autodetects input format
  // outputs stream s bam to StdOut
  def pipeToBam = cmd"$samtools -Sb -"   
  
}
