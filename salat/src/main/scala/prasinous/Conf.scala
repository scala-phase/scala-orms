package prasinous

import com.mongodb.casbah.commons.Logging
import net.lag.configgy.Config
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import com.mongodb.{MongoOptions, ServerAddress, Mongo}

trait Conf extends Logging {

  val confFile: String = "phase.conf"

  lazy private val cl = getClass.getClassLoader

  lazy val Configuration = {
    try {
      val config = Config.fromResource(confFile, cl)
      log.info("loaded config from %s", cl.getResource(confFile))
      config
    }
    catch {
      case t => {
        val urls = cl.asInstanceOf[java.net.URLClassLoader].getURLs.filter(!_.toString.endsWith(".jar"))
        throw new RuntimeException("failed to load %s from classpath:\n%s".format(confFile, urls.mkString("\n")), t)
      }
    }
  }
}

class MissingPropertyError(property: String) extends Error("Missing property: %s".format(property))

object Mongo extends Conf {
  private def mongo(name: String): (MongoConnection, MongoDB) = {
    val conn = new MongoConnection(
      new Mongo(
      new ServerAddress(
        Configuration.getString("mongodb.hostname".format(name)).getOrElse(
          throw new IllegalArgumentException("'mongodb.%s.%s' needs a 'hostname' setting".format(name))
        ),
        Configuration.getInt("mongodb.port".format(name)).getOrElse(
          throw new IllegalArgumentException("'mongodb' needs a 'port' setting".format(name))
        )
      ), {
        val opts = new MongoOptions
        opts.connectionsPerHost = 50
        opts.autoConnectRetry = true
        opts
      }
      )
    )
    conn -> conn(Configuration.getString("mongodb.db".format(name)).
      getOrElse(throw new IllegalArgumentException("'mongodb' needs a 'db' setting".format(name))))
  }

  val Library = mongo("library")._2
}


