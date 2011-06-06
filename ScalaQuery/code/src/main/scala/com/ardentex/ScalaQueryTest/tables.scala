package com.ardentex.ScalaQueryTest

import org.scalaquery.ql.basic.{BasicTable => Table}
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql._

object Author
extends Table[(Int, String, String, Option[String], String, Option[String])]("AUTHOR")
{
    def id = column[Int]("id", O NotNull, O PrimaryKey)

    def firstName = column[String]("first_name",
                                   O NotNull,
                                   O DBType "varchar(50)")

    def lastName = column[String]("last_name",
                                  O NotNull,
                                  O DBType "varchar(50)")

    def middleName = column[Option[String]]("middle_name",
                                            O DBType "varchar(50)")

    def nationality = column[String]("nationality",
                                     O Default "US",
                                     O DBType "varchar(100)")

    def birthYear = column[Option[String]]("year_of_birth",
                                           O DBType "varchar(4)")

    def * = id ~ firstName ~ lastName ~ middleName ~ nationality ~ birthYear
}
