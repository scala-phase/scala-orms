package com.ardentex.ScalaQueryTest

import org.scalaquery.ql.extended.SQLiteDriver.Implicit._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

object ShowAuthors
{
    def main(args: Array[String])
    {
        val db = Database.forURL("jdbc:sqlite:testdb.sqlite3",
                                 driver = "org.sqlite.JDBC")
        val nameQuery = for {a <- Author if (a.nationality === "US")}
                          yield a.id ~ a.lastName ~ a.firstName
        db withSession
        {
            val list: List[(Int, String, String)] = nameQuery.list
            for ((id, last, first) <- list)
                println("%02d: %s, %s".format(id, last, first))
        }
    }
}
