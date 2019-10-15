package zflow

import shapeless._

object model {

  sealed trait PathExtendable[A] {
    def / (value: A): PathType[A]
  }

  sealed trait PathType[A] {
    def path(a: A): String
  }

  object PathType {

    def apply[A](implicit enc: PathType[A]): PathType[A] =
      enc

    def instance[A](func: A => String): PathType[A] = new PathType[A] {
      def path(value: A): String = 
        func(value)
    }
      
    implicit val stringHelper: PathType[String] = 
      instance(s => s)
    
    implicit val hnilEncoder: PathType[HNil] =
      instance(_ => "")

    // hnilEncoder and hlistEncoder are able to 
    // derive paths from all the above definitions
    implicit def hlistEncoder[H, T <: HList]  (
      implicit
      hEncoder: Lazy[PathType[H]],
      tEncoder: PathType[T]
    ): PathType[ H :: T] =
      instance {
        case h :: t =>
          hEncoder.value.path(h) + tEncoder.path(t)
      }
    
    implicit def genericPathHelper[A, R](
      implicit
      gen: Generic.Aux[A, R],
      enc: Lazy[PathType[R]]
    ): PathType[A] = 
      instance( a => enc.value.path(gen.to(a)))

  implicit val cnilEncoder: PathType[CNil] =
    instance(_ => throw new Exception("Inconceivable"))

  implicit def coproductEncoder[H, T <: Coproduct](
    implicit
    hEncoder: Lazy[PathType[H]],
    tEncoder: PathType[T]
  ): PathType[H :+: T] = instance {
    case Inl(h) => hEncoder.value.path(h)
    case Inr(t) => tEncoder.path(t)
   }
  }

  sealed trait CmdArg extends Product with Serializable
  case class Arg(s: String) extends CmdArg {
    def stripMargin = copy(
      s = s.stripMargin('|')
    )
  }

  case class MultiArg(
    args: List[CmdArg]
  ) extends CmdArg

  sealed trait PathOps extends CmdArg
    
  sealed trait PathAppendable extends PathOps {

    def / (p: PathTerminator): PathTerminator = 
      FilePath(path(this) + "/" + path(p))
    
    def / (p: PathAppendable): PathAppendable = 
      FolderPath(path(this) + "/" + path(p))
  }

//  case class PathValue(value: String) extends AnyVal
  case class FileName(value: String) extends PathTerminator
  case class FolderName(value: String) extends PathAppendable

  sealed trait PathTerminator extends PathOps
  case class FilePath(value: String) extends PathTerminator {
    def fileName = value.reverse.takeWhile(_ != '/').reverse
    def folderPath = value.reverse.dropWhile(_ != '/').drop(1).reverse
  }

  case class FolderPath(value: String) extends PathAppendable 
  case class SubFolderPath(value: String) extends PathAppendable

  sealed abstract class BioFile extends CmdArg
  case class Bam(value: FilePath) extends BioFile with PathTerminator
  case class Cram(value: FilePath) extends BioFile with PathTerminator
  case class Ref(value: FilePath) extends BioFile with PathTerminator
  case class FastQ(value: FilePath) extends BioFile with PathTerminator
  
  case class BamFileName(value: FileName) extends BioFile
  case class BamFolder(value: FolderPath) extends BioFile
  case class CramFileName(value: FileName) extends BioFile
  case class CramFolder(value: FolderPath) extends BioFile
  case class FastQFileName(value: FileName) extends BioFile
  case class FastQFolder(value: FolderPath) extends BioFile
  case class RefFileName(value: FileName) extends BioFile
  case class RefFolder(value: FolderPath) extends BioFile
  
  case class WorkDir(value: FolderPath) extends CmdArg
  case class JobDir(value: FolderPath) extends CmdArg
  case class JobName(value: String) extends CmdArg
  case class SampleName(value: String) extends CmdArg

  object FileUtils {

    def extendFileName(s: String, p: FilePath): FilePath = {
      val ext = p.value.reverse.take(4).reverse
      FilePath(s"${p.value.dropRight(4)}${s}$ext")
      }
  }

  case class Vcf(value: FilePath) extends PathTerminator {
    def fileName = value.fileName
    def folderPath = value.folderPath
    def extendFileName(s: String) = 
      this.copy(value = FileUtils.extendFileName(s, value))
  }


  case class Bed(value: FilePath) extends PathTerminator {
    def fileName = value.fileName
    def folderPath = value.folderPath
    def extendFileName(s: String) = {
      val ext = value.value.reverse.take(4).reverse
      this.copy(
      value = FilePath(s"${value.value.dropRight(4)}${s}$ext")
    )}
    def mergeBedFileNames(bed: Bed) = this.copy(
      value = FilePath(s"${value.folderPath}/${value.fileName.dropRight(4)}_${bed.fileName}")
    )
  }

  sealed trait Command extends CmdArg
  case class Cmd(args: List[String]) extends Command {
    def & = copy( args = args :+ "&")

    def toCmd = args.mkString(" ")
    def toSudoCmd = ("sudo" +: args).mkString(" ")

  }

  case class Bash(script: String, name: String, preCmd: Cmd) extends Command {

    def stripMargin = copy(
      script = script.stripMargin('|')
    )

  }

  case class SBash(
    script: String, 
    name: String,
    runCmd: Cmd,
    env: Option[String] = None
  ) extends Command {

    def stripMargin = copy(
      script = script.stripMargin('|')
    )
  }

  case class SlurmConfig(
    name: String,
    cpusPrTask: Int,
    memPrCpu: Int,
    timeout: String,
    account: String = "Undefined"
  )

  trait JobResult[A]{
    def get: A
  }

  case class Job(
    cmds: Command*,
  )(
    implicit
    name: sourcecode.Name,
    workdir: WorkDir
  ) {
    import syntax._
    val jobName: JobName = JobName(name.value)
    val jobDir = path"$workdir/$jobName"
  }

  def path[A](pathType: A)(implicit toPath: PathType[A]): String = {
    toPath.path(pathType)
  }

  case class SlurmScript(
    scriptPath: os.Path,
    scriptFolder: os.Path
  )

  case class SlurmJob private (
    cmds: List[Command],
    workdir: FolderPath,
    jobName: String,
    timeout: String
  ) {

    def logPath: FilePath = 
      FilePath(s"${workdir.value}/${jobName}.log}")
  }

  object SlurmJob {

    def apply(timeout: String, cmds: Command*)(
      implicit 
      name: sourcecode.Name, 
      baseFolder: FolderPath
    ): SlurmJob = 
      SlurmJob(cmds.toList, FolderPath(s"${baseFolder.value}/${name.value}"), name.value, timeout)

  }
}