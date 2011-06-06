package com.ardentex.ScalaQueryTest

import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.ql.Query
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

object CountAuthorsPerBook extends TestConfig
{
    def main(args: Array[String])
    {
        val q = for {ba <- BookAuthor
                     b  <- Book if (ba.bookID === b.id)
                         _ <- Query groupBy ba.bookID}
                    yield ba.authorID.count ~ b.title

        db withSession
        {
            for ((count, title) <- q.list)
                printf("%s: %d author(s)\n", title, count)
        }
    }
}
