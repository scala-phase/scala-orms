package com.ardentex.ScalaQueryTest

import org.scalaquery.ql._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

// Demonstrates an explicit join.
object ShowAuthorsWithBooks extends TestConfig
{
    def main(args: Array[String])
    {
        db withSession
        {
            // An explicit join. NOTE: Authors with multiple books are listed
            // multiple times.

            val q2 = 
                for {Join(a, ab) <- Author innerJoin BookAuthor on (_.id is _.authorID)}
                    yield a.lastName ~ a.firstName
            for ((last, first) <- q2.list)
                printf("%s, %s\n", last, first)
        }
    }
}
