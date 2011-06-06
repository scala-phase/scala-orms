package com.ardentex.ScalaQueryTest

import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

object ShowAuthorsByNationality extends TestConfig
{
    def main(args: Array[String])
    {
        var nationality = if (args.length == 0) "US" else args(0)
        val nameQuery = for {a <- Author if (a.nationality === nationality)}
                          yield a.id ~ a.lastName ~ a.firstName
        db withSession
        {
            val list: List[(Int, String, String)] = nameQuery.list
            for ((id, last, first) <- list)
                printf("%02d: %s, %s\n", id, last, first)
        }
    }
}
