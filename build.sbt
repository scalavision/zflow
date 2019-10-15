val stdOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-explaintypes",
  "-Yrangepos",
  "-feature",
  "-Xfuture",
  "-Ypartial-unification",
  "-language:higherKinds",
  "-language:existentials",
  "-unchecked",
  "-Yno-adapted-args",
  "-Xlint:_,-type-parameter-shadow",
  "-Xsource:2.13",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfatal-warnings"
)

def extraOptions(scalaVersion: String) =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 12)) =>
      Seq(
        "-opt-warnings",
        "-Ywarn-extra-implicit",
        "-Ywarn-unused:_,imports",
        "-Ywarn-unused:imports",
        "-opt:l:inline",
        "-opt-inline-from:<source>"
      )
    case _ =>
      Seq(
        "-Xexperimental",
        "-Ywarn-unused-import"
      )
  }

def stdSettings(prjName: String) = Seq(
    name := s"ZFlow",
    scalacOptions := stdOptions,
    crossScalaVersions := Seq("2.12.10"),
    maxErrors := 5,
    triggeredMessage := Watched.clearWhenTriggered,
//    scalaVersion in ThisBuild := crossScalaVersions.value.head,
    scalacOptions := stdOptions ++ extraOptions(scalaVersion.value),
//    libraryDependencies ++= compileOnlyDeps ++ testDeps ++ Seq(
    libraryDependencies ++= Seq(
      compilerPlugin("org.spire-math"         %% "kind-projector"  % "0.9.7"),
      compilerPlugin("com.github.tomasmikula" %% "pascal"          % "0.2.1"),
      //compilerPlugin("com.github.ghik"        %% "silencer-plugin" % "1.0")
    ),
    incOptions ~= (_.withLogRecompileOnMacro(false))
)

lazy val projectName = "ZFlow"

lazy val root = project
  .in(file("."))
  .settings(stdSettings(projectName))
  .settings(
    assemblyJarName in assembly := "zflow.jar",
    mainClass in assembly := Some("attempt1.Main")
  )
  .settings(
    libraryDependencies ++= Seq(
      
      "com.monovore" %% "decline" % "0.5.0",
      "dev.zio" %% "zio" % "1.0.0-RC12-1",
      "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC3",
      "co.fs2" %% "fs2-core" % "1.0.4",
      "org.libreoffice" % "unoil" % "6.2.3",
      "org.libreoffice" % "juh" % "6.2.3",
      "org.libreoffice" % "officebean" % "6.2.3",
      // This seems to do the magic ...
      "org.libreoffice" % "ridl" % "6.2.3",
        // java.lang.NoClassDefFoundError: com/sun/star/comp/servicemanager/ServiceManager
      "org.libreoffice" % "jurt" % "6.2.3",
      "org.libreoffice" % "unoloader" % "6.2.3",
      "com.github.jeremysolarz" % "bootstrap-connector" % "1.0.0",
      "co.fs2" %% "fs2-io" % "1.0.4",
      "org.scalaz" %% "scalaz-core" % "7.2.27",
      "com.lihaoyi" %% "sourcecode" % "0.1.4",
      "com.lihaoyi" %% "pprint" % "0.5.3",
      "com.lihaoyi" %% "os-lib" % "0.2.8",
      "com.chuusai" %% "shapeless" % "2.3.3",
      "com.github.scopt" %% "scopt" % "4.0.0-RC2",
      "org.wvlet.airframe" %% "airframe-log" % "0.78",
      "dev.zio" %% "zio-test" % "1.0.0-RC11-1" % Test,
      "org.specs2" %% "specs2-core"          % "4.4.1" % Test,
      "org.specs2" %% "specs2-scalacheck"    % "4.4.1" % Test,
      "org.specs2" %% "specs2-matcher-extra" % "4.4.1" % Test,
      "org.specs2" %% "specs2-scalaz"        % "4.4.1" % Test,
      //"org.typelevel" %%% "cats-effect-laws" % "1.1.0" % "test",
      "org.typelevel" %% "cats-effect" % "1.2.0",
      "org.typelevel" %% "cats-mtl-core" % "0.4.0",
      "org.typelevel" %% "cats-core" % "1.6.0",
      "com.github.samtools" % "htsjdk" % "2.19.0"
      // "org.scalatest" %%% "scalatest" % "3.0.5" % "test",
      // "org.scalacheck" %%% "scalacheck" % "1.13.5" % "test"
    ),
    scalacOptions in Test ++= Seq("-Yrangepos")
    //publishLocal in ThisBuild := 
    //  Some(Resolver.file("file", new File("../gflow/scripts/lib/")))
  )
  .settings(
    // In the repl most warnings are useless or worse.
    // This is intentionally := as it's more direct to enumerate the few
    // options we do want than to try to subtract off the ones we don't.
    // One of -Ydelambdafy:inline or -Yrepl-class-based must be given to
    // avoid deadlocking on parallel operations, see
    //   https://issues.scala-lang.org/browse/SI-9076
    scalacOptions in Compile in console := Seq(
      "-Ypartial-unification",
      "-language:higherKinds",
      "-language:existentials",
      "-Yno-adapted-args",
      "-Xsource:2.13",
      "-Yrepl-class-based"
    ),
    initialCommands in Compile in console := """
                                               |import scalaz._
                                               |import scalaz.zio._
                                               |import scalaz.zio.console._
                                               |object replRTS extends RTS {}
                                               |import replRTS._
                                               |implicit class RunSyntax[E, A](io: IO[E, A]){ def unsafeRun: A = replRTS.unsafeRun(io) }
    """.stripMargin
  )
/*  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, isSnapshot),
    buildInfoPackage := "scalaz.zio",
    buildInfoObject := "BuildInfo"
  )*/
/*
lazy val root = project.in(file("."))
  .aggregate(Attempt1)
*/
