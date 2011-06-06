package com.ardentex.ScalaQueryTest

//import org.scalaquery.ql.basic.{BasicTable => Table}
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import org.scalaquery.ql.extended.SQLiteDriver.Implicit._
import org.scalaquery.ql.extended.SQLiteDriver._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql._
import org.scalaquery.session._

object Author
extends Table[(Int, String, String, Option[String], String, Option[String])]("AUTHOR")
{
    def id = column[Int]("id", O NotNull, O AutoInc, O PrimaryKey)

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

object Book
extends Table[(Int, String)]("BOOK")
{
    def id = column[Int]("id", O NotNull, O AutoInc, O PrimaryKey)
    def title = column[String]("title", O NotNull, O DBType "varchar(100)")

    def * = id ~ title
}

/**
 * Intersection table between authors and books.
 */
object BookAuthor extends Table[(Int, Int)]("BOOKAUTHOR")
{
    def authorID = column[Int]("author_id", O NotNull)
    def bookID = column[Int]("book_id")

    def fkAuthorID = foreignKey("fk_author_id", authorID, Author)(_.id)
    def fkBookID = foreignKey("fk_book_id", bookID, Book)(_.id)

    def * = authorID ~ bookID
}
