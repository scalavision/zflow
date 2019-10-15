package zflow.tools

import zflow.syntax._
import zflow.model._


object picard {

  val picard = arg"picard"

  def markDuplicates(
    in: Bam,
    out: Bam,
    markedDuplicateMetrics: FilePath
  ) = 
    cmd"$picard MarkDuplicated I=$in O=$out M=$markedDuplicateMetrics"

}