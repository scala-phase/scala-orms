package com.ardentex.ScalaQueryTest

import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

object InsertAuthor extends TestConfig
{
    def main(args: Array[String])
    {
        if (args.length != 2)
            println("Usage: InsertAuthor last_name first_name")
        else
            doInsert(args(0), args(1))
    }

    private def doInsert(lastName: String, firstName: String) =
    {
        db withSession
        {
            Author.lastName ~
            Author.firstName ~
            Author.middleName insert (lastName, firstName, None)
        }
    }
}
