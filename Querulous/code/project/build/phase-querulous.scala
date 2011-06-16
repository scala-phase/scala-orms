import sbt._
import java.io.File

class PhaseQuerulousProject(info: ProjectInfo) extends DefaultProject(info)
{
  val querulous = "com.twitter" % "querulous" % "1.2.0-generic"
//  val scalaqlite = "org.srhea" % "scalaqlite" % "0.1.0-SNAPSHOT"

  val novusRels = "novus rels" at "http://repo.novus.com/releases"
  val novusSnaps = "novus snaps" at "http://repo.novus.com/snapshots"
}



