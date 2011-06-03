import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {

  val scalaToolsSnapshots = (
    "Scala-Tools Maven2 Snapshots Repository" at
    "http://scala-tools.org/repo-snapshots"
  )
   
  val squeryl = "org.squeryl" %% "squeryl" % "0.9.5-SNAPSHOT"
  log.setLevel(Level.Warn)
  log.setTrace(2)
}
