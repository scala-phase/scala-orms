import java.util.Date
import java.sql.{ DriverManager, Timestamp }

import org.squeryl.{ KeyedEntity, Schema, Session, SessionFactory }
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.MySQLInnoDBAdapter
import org.squeryl.annotations.Column


/**
 * Advantages of Squeryl:
 * 
 * - Static checking of SQL. This is huge!! If your code compiles, it's *very*
 * likely the SQL will not crash at run-time.
 * 
 * - Easy refactoring. You only need to change column/table/database names in
 * *one* place.
 *
 * - Maintenance and evolution. The db can change without you constantly
 * fearing that your SQL no longer works.
 *
 * - Security. Squeryl uses JDBC setObject with types, exclusively. Your code
 * is as safe from injection attacks as any JDBC prepared statement.
 *
 * - Portability. SQL is probably the lease portable widely-used language.
 * Squeryl supports all the major vendors, while giving you powerful tools
 * that go way beyond what the core/portable SQL provides. You write your code
 * once and let Squeryl worry about all the vendor idiosyncracies.
 */

object Main {

  /* Connect to the database. Squeryl supports all the big ones. */
  Class.forName("com.mysql.jdbc.Driver");
  SessionFactory.concreteFactory = Some( () =>
    Session.create(
    DriverManager.getConnection("jdbc://localhost:3306", "yuvi",
                                "password"),
    new MySQLInnoDBAdapter))

  def main(args: Array[String]) {
    val q = new Querier()
    //q.someQuery()
  }
}

class Querier() {
  import LibrarySchema._

  //this is a handy logging function. put this inside any transaction {}
  //block and the generated SQL will be printed to stdout!
  val printSql = {
    () => Session.currentSession.setLogger( (s: String) => println(s) )
  }

  def bookTitles(): List[String] = {
    transaction {
      from(bookTb) ( b =>
        select(b.title)
      ).toList
    }
  }

  def booksBy(lastName: String): List[Long] = {
    transaction {
      from(bookTb, bookAuthorTb, authorTb) ( (b, ba, a) =>
        where(
          b.id === ba.bookId and
          a.id === ba.authorId and
          a.lastName === lastName
        )
        select(b.id)
      ).toList
    }
  }

  //return the `id` of the new author row
  def insertNewAuthor(lastName: String, firstName: String): Long = {
    transaction {
      printSql() //so we can debug the generated SQL
      val auth = new Author(lastName, firstName, None, None, None)
      authorTb.insert(auth)
      auth.id //wtf, Squeryl re-assigned to Author.id val using reflection!
    }
  }
}

/* Wrap the tables into a `Schema` and declare some properties. */

object LibrarySchema extends Schema {

  val authorTb = table[Author]("author")
  on(authorTb) { c =>
    declare(
      //Strings default to VARCHAR(128) which is usually fine. If you really
      //want to specify your database's data type more exactly, here's how
      c.yearOfBirth is dbType("VARCHAR(4)"),
      c.id is autoIncremented
    )
  }

  val bookTb = table[Book]("book")
  on(bookTb) { c => declare(c.id is autoIncremented) }

  val bookAuthorTb = table[BookAuthor]("bookauthor")

  val borrowerTb = table[Borrower]("borrower")
  on(borrowerTb) { c => declare(c.id is autoIncremented) }

  val borrowalTb = table[Borrowal]("borrowal")
  on(borrowalTb) { c => declare(c.id is autoIncremented) }
}

/* Declare the tables. */

class Author(
  @Column("last_name")
  val lastName: String,
  @Column("first_name")
  val firstName: String,
  @Column("middle_name")
  val middleName: Option[String],
  val nationality: Option[String],
  @Column("year_of_birth")
  val yearOfBirth: Option[String]
) extends KeyedEntity[Long] {
  val id: Long = -1L
}

class BookAuthor(
  @Column("book_id")
  val bookId: Long,
  @Column("author_id")
  val authorId: Long
)

class Book(
  val title: String
) extends KeyedEntity[Long] {
  val id: Long = -1L
}

class Borrower(
  @Column("phone_num")
  val phoneNum: String,
  val address: String
) extends KeyedEntity[Long] {
  val id: Long = -1L
}

class Borrowal(
  @Column("book_id")
  val bookId: Long,
  @Column("borrower_id")
  val borrowerId: Long,
  @Column("scheduled_to_return_on")
  val scheduledToReturnOn: Date,
  @Column("returned_on")
  val returnedOn: Option[Timestamp],
  @Column("num_nonreturn_phonecalls")
  val numNonReturnPhonecalls: Int
) extends KeyedEntity[Long] {
  val id: Long = -1L
}
