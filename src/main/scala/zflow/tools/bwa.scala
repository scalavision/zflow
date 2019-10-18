package zflow.tools

import zflow.syntax._
import zflow.model._

object bwa {

  val bwa = arg"bwa"

  def map(
    ref: Ref,
    read1: FastQ,
    read2: FastQ
  ) = cmd"$bwa mem $ref $read1 $read2"

  def mapInMemory(
    ref: Ref,
    read1: FastQ,
    read2: FastQ,
    header: String,
    cores: Int
  ) = {
    val hdr = arg"$header"
    val crs = arg"$cores"
    cmd"$bwa mem -R $hdr -M -t $crs $ref $read1 $read2"
  }
  
  def indexRef(
    ref: Ref
  ) = cmd"$bwa index $ref"
  
}
