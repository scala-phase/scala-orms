package com.ardentex.ScalaQueryTest

import scala.collection.immutable.WrappedString
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.ql._
import org.scalaquery._
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

object ShowAuthorsByNameAndCountry extends TestConfig
{
    def main(args: Array[String])
    {
        def usage =
            println("Usage: ShowAuthorsByName lastName [country]")

        args.toList match
        {
            case l :: cc :: Nil => run(l, cc);
            case l ::Nil        => run(l);
            case _              => usage
        }
    }

    private def run(lastName: String, countryCode: String) =
    {
        // Create the parameterized query template.
        val qt = for {lnc ~ cc <- Parameters[String, String]
                      a <- Author if a.lastName === lnc && a.nationality === cc}
                     yield a.id ~ a.lastName ~ a.firstName ~ a.nationality
        
        // Instantiate and run.
        val q = qt((lastName, countryCode))
        db withSession
        {
            for (rs <- q.list)
                show(rs)
        }
    }

    private def run(lastName: String) =
    {
        // Create the parameterized query template.
        val qt = for {lnc <- Parameters[String]
                      a <- Author if a.lastName === lnc}
                     yield a.id ~ a.lastName ~ a.firstName ~ a.nationality
        
        // Instantiate and run.
        val q = qt(lastName)
        db withSession
        {
            for (rs <- q.list)
                show(rs)
        }
    }

    private def show(rs: (Int, String, String, String))
    {
        printf("%02d: %s %s (%s)\n", rs._1, rs._3, rs._2, rs._4)
    }

}
