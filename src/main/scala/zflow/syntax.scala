package zflow

import model._
import scala.language.implicitConversions

object syntax {

  implicit class BioinfSyntax(private val s: StringContext) {
    import CmdStringify._

    // def bam(args: Any*): Bam =  Bam(os.Path(s.s(args :_*)))

    // def ref(args: Any*): Ref = Ref(os.Path(s.s(args :_*)))

    def cmd(args: CmdArg*): Cmd = {
      val params = args.map(_.stringify)
      Cmd(s.s(params: _*).split(" ").toList)
    } 

    def sh(args: CmdArg*)(implicit name: sourcecode.Name): Bash = {
      val nameValue = arg"$name"
      val params = args.map(_.stringify)
      Bash(s.s(params :_*), name.value, cmd"$nameValue")
    }

    def sample(args: CmdArg*): SampleName = {
      val params = args.map(_.stringify)
      SampleName(s.s(params: _*))
    }

    def job(args: CmdArg*): JobDir = {
      val params = args.map(_.stringify)
      JobDir(FolderPath(s.s(params :_*)))
    }
    // def bamDir(args: Any*): BamDir = 
    //   BamDir(os.Path(s.s(args:_*)))

    // def refDir(args: Any*): RefDir = 
    //   RefDir(os.Path(s.s(args:_*)))

    def fileName(args: CmdArg*): FileName = {
      val params = args.map(_.stringify)
      FileName(s.s(params: _*))
    }

    def sampleName(args: CmdArg*): SampleName = {
      val params = args.map(_.stringify)
      SampleName(s.s(params: _*))
    }

    def workDir(args: CmdArg*): WorkDir = {
      val params = args.map(_.stringify)
      WorkDir(FolderPath(s.s(params: _*)))
    }
    
    def jobDir(args: CmdArg*): JobDir = {
      val params = args.map(_.stringify)
      JobDir(FolderPath(s.s(params: _*)))
    }

    def arg(args: Any*): Arg = 
      Arg(s.s(args: _*))

    def path(args: CmdArg*): FolderPath = {
      val params = args.map(_.stringify)
      FolderPath(s.s(params: _*))
    }

    def file(args: CmdArg*): FilePath = {
      val params = args.map(_.stringify)
      FilePath(s.s(params: _*))
    }
    
     def vcf(args: CmdArg*): Vcf =  {
      val params = args.map(_.stringify)
      Vcf(FilePath(s.s(params: _*)))
     }
     
     def bed(args: CmdArg*): Bed =  {
      val params = args.map(_.stringify)
      Bed(FilePath(s.s(params: _*)))
     }
    
     def bam(args: CmdArg*): Bam =  {
      val params = args.map(_.stringify)
      Bam(FilePath(s.s(params: _*)))
     }
  }

  def o (implicit name: sourcecode.Name): String = 
    name.value

  def arg(implicit name: sourcecode.Name): String = 
    s"--${name.value}"

  implicit class PathSyntax[A](a: A) {

    def path(implicit toPath: PathType[A]): String = 
      toPath.path(a)

//    def toOsPath = os.Path(a.path)

  }

  implicit class ArgSyntax(s: String) {
    def toArg = Arg(s)
    def toSample = SampleName(s)
    def toBed = Bed(FilePath(s))
    def toFile = FilePath(s)
  }

  implicit class OsLibSyntaxFilePath(filePath: FilePath) {
    def toPath = os.Path(filePath.value)
  }

  implicit class OsLibSyntaxFolderPath(folderPath: FolderPath) {
    def toPath = os.Path(folderPath.value)
  }

  implicit class OsLibPath(p: os.Path) {
    def toVcf = Vcf(FilePath(p.toString()))
    def toBed = Bed(FilePath(p.toString()))
    def toBam = Bed(FilePath(p.toString()))
    def toFasta = Bed(FilePath(p.toString()))
    def toFastQ = Bed(FilePath(p.toString()))
    def toFile = FilePath(p.toString())
  }

  implicit def convertListOfArgsToCmdArg(in: Seq[Vcf]): CmdArg = {
    MultiArg(in.toList)
  }

}