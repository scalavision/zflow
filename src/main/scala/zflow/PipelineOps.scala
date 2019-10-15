package zflow

import scalaz._
import Scalaz._

import zflow.model._

case class Step(
  cmds: Vector[Cmd] = Vector.empty[Cmd],
  out: List[PathOps] = List.empty[PathOps]
)

trait PipelineOps {

  def create: State[Step, Step] = for {
    _ <- init
    p <- get
  } yield p

  def addStep(cmd: Cmd, out: List[PathOps]): Step => Step = 
    pipeline => pipeline.copy(
      cmds = pipeline.cmds :+ cmd,
      out = out
    )
  
  def step(
    cmd: Cmd,
    out: List[PathOps] = List.empty[PathOps]
  ): State[Step, Step] = for {
    _ <- init
    _ <- modify(addStep(cmd, out))
    p <- get
  } yield p

  def addSteps(
      cmd: Vector[Cmd],
      out: List[PathOps]
    ): Step => Step =
      pipeline => pipeline.copy(
        cmds = pipeline.cmds ++ cmd.toVector,
        out = out
      )
  
  def steps(cmds: List[Cmd], out: List[PathOps] = List.empty[PathOps]): State[Step, Step] = for {
    _ <- init
    _ <- modify(addSteps(cmds.toVector, out))
    p <- get
  } yield p

}
