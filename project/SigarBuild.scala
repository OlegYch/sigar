import sbt.Keys._
import sbt._

object SigarBuild extends Build {
  lazy val root = project.in(file(".")).aggregate(sigar).settings(commonSettings)

  lazy val dist = taskKey[Int]("call ant to build java bindings")
  lazy val packageNative = taskKey[File]("create a zip with native lib for current platform")

  def ant(task: String) = Def.task {
    val ant = if (scala.util.Properties.isWin) "ant.bat" else "ant"
    Process(ant + s" $task", baseDirectory.value) !
  }

  def outputDir = Def.setting(baseDirectory.value / "sigar-bin" / "lib")

  lazy val sigar = project.in(file("bindings/java")).settings(commonSettings).settings(
    name := "sigar"
    , version := "1.7.0-OLEGYCH-SNAPSHOT"
    , dist := ant("build dist").value
    , clean := {
      clean.value
      ant("clean").value
    }
    , packageBin in Compile := {
      dist.value
      outputDir.value / "sigar.jar"
    }
    , packageNative := {
      dist.value
      val files = outputDir.value ** ("*.so" || "*.dll" || "*.dylib")
      val outputZip = outputDir.value / "native.zip"
      IO.zip(files.get.map(f => f -> f.getName), outputZip)
      outputZip
    }
    , addArtifact(Artifact("sigar", "zip", "zip", "libs"), packageNative)
  )

  def commonSettings = Seq(
    licenses +=("Apache 2", url("http://www.apache.org/licenses/LICENSE-2.0"))
    , organization := "org.hyperic"
    , autoScalaLibrary := false
    , crossPaths := false
  )
}
