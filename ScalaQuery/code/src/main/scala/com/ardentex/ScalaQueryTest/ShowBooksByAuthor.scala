package com.ardentex.ScalaQueryTest

import org.scalaquery.ql._
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

// Demonstrates an implicit join.
object ShowBooksByAuthor extends TestConfig
{
    def main(args: Array[String])
    {
        db withSession
        {
            // An implicit join.
            val q1 = for {a <- Author
                          b <- Book
                          ab <- BookAuthor if (ab.bookID === b.id) &&
                                              (ab.authorID === a.id)}
                         yield a.lastName ~ a.firstName ~ b.title

            for ((last, first, title) <- q1.list)
                printf("%s, %s: %s\n", last, first, title)
        }
    }
}
