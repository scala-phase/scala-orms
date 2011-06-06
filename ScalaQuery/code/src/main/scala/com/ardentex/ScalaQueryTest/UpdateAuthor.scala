package com.ardentex.ScalaQueryTest

import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

object UpdateAuthor extends TestConfig
{
    def main(args: Array[String])
    {
        if (args.length != 2)
            println("Usage: UpdateAuthor last_name_to_find new_nationality")
        else
            doUpdate(args(0), args(1))
    }

    private def doUpdate(lastNameToFind: String, newNationality: String) =
    {
        val q = for {a <- Author if a.lastName === lastNameToFind}
                    yield a.firstName ~ a.lastName ~ a.nationality

        db withSession
        {
            show("*** BEFORE:", lastNameToFind)

            val q = for {a <- Author if a.lastName === lastNameToFind}
                         yield a.nationality
            q.update(newNationality)

            show("*** AFTER:", lastNameToFind)
        }
    }

    private def show(prefix: String, lastName: String) = {
        val q = for {a <- Author if a.lastName === lastName}
                    yield a.firstName ~ a.lastName ~ a.nationality
        println(prefix)
        for (rs <- q.list)
            printf("%s %s: %s", rs._1, rs._2, rs._3)
   }
}
