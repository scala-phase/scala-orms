package com.ardentex.ScalaQueryTest

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

trait TestConfig
{
    def db = Database.forURL("jdbc:sqlite:testdb.sqlite3",
                             driver = "org.sqlite.JDBC")
}
