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

}