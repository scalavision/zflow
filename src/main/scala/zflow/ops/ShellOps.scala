package zflow.ops

import zflow.model._

object ShellOps {
  
  def cmdsToScript(
    cmds: List[Cmd],
    scriptEnv: String = "#!/usr/bin/env bash"
  ): String =  {

    val scriptBody = cmds.map(_.args.mkString(" ")).mkString("\n")
    
    s"""|$scriptEnv
    |$scriptBody
    |""".stripMargin

  }

  def saveScript(
    scriptPath: FilePath,
    scriptContent: String
  ): FilePath =  {
    os.write.over(
      os.Path(
        scriptPath.value
      ),
        scriptContent
      )
    scriptPath
  }

  def runAsScript(
    cmds: List[Cmd],
    scriptPath: FilePath,
    scriptEnv: String = "#!/usr/bin/env bash"
  ) = {

    val wd = scriptPath.folderPath
    val script = cmdsToScript(cmds, scriptEnv)
    saveScript(scriptPath, script)
    os.proc(script).call(os.Path(wd))

  }

  case class ProcOutput(
    out: List[String],
    err: List[String]
  )

  def collectOutput(
    cmd: Cmd
  ): ProcOutput = {

    var in = ""
    var err = ""
    val cmdArgs: List[String] = cmd.args

    println(cmd.toCmd)

    os.proc(cmdArgs).stream(
      onOut = (buf, _) => 
        in += buf.map(_.toChar).mkString, 
      onErr = (buf, _) =>  {
        err += buf.map(_.toChar).mkString
      }
    )

    ProcOutput(
      in.split("\n").toList.filter(_.nonEmpty).filterNot(_.startsWith("\u0000")),
      err.split("\n").toList.filter(_.nonEmpty)
    )

  }

  

}
