!SLIDE title-page

# The Scala Query ORM

Brian Clapper, *bmc@ardentex.com*

ArdenTex, Inc.

*16 July, 2011*

!SLIDE smbullets incremental transition=fade

# The Scala Query ORM

- ScalaQuery is "an API / DSL built on top of JDBC".
- Provides compile-time checking and type-safety for queries
- (Database entities have static types.)
- Composable, non-leaky abstractions
- Relational algebra and query comprehensions
- Can be composed, the way one can compose Scala's collection classes.
- Does not rely on mutable state.
- Supports PostgreSQL, MySQL, H2, HSQLDB/HyperSQL, Derby/JavaDB,
  MS SQL Server, MS Access, and SQLite.
  
!SLIDE transition=fade

# Making a Connection
  
ScalaQuery requires an underlying JDBC connection.

All DB calls go through a `Session`, which is obtained from a `Database`:

    import org.scalaquery.session._
    import org.scalaquery.session.Database.threadLocalSession

    val db = Database.forURL(
      "jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1",
      driver = "org.h2.Driver"
    )

!SLIDE transition=fade

# Using the Session

Implicitly:

    val myQuery = ...
    db withSession {
      myQuery.list()
    }
    
Explicitly:

    val myQuery = ...
    db withSession { session =>
      myQuery.list()(session)
    }

!SLIDE transition=fade

# Tables: Schema, redux

Our schema, again (in SQLite-speak):

    CREATE TABLE author (
      id             INTEGER PRIMARY KEY,
      last_name      VARCHAR(50) NOT NULL,
      first_name     VARCHAR(50) NOT NULL,
      middle_name    VARCHAR(50) NULL,
      nationality    VARCHAR(100) DEFAULT 'US',
      year_of_birth  VARCHAR(4)
    );

    CREATE TABLE book (
      id           INTEGER PRIMARY KEY,
      title        VARCHAR(100) NOT NULL,
      author_id    INTEGER NOT NULL,
      co_author_id INTEGER,

      FOREIGN KEY (author_id) REFERENCES author(id),
      FOREIGN KEY (co_author_id) REFERENCES author(id)
    );

!SLIDE transition=fade

# Tables: Schema, redux

    CREATE TABLE borrower (
      id        INTEGER PRIMARY KEY,
      phone_num VARCHAR(20) NOT NULL,
      address   TEXT NOT NULL
    );

    CREATE TABLE borrowal (
      id                       INTEGER PRIMARY KEY,
      book_id                  INTEGER NOT NULL,
      borrower_id              INTEGER NOT NULL,
      scheduled_to_return_on   DATE NOT NULL,
      returned_on              TIMESTAMP,
      num_nonreturn_phonecalls INT,

      FOREIGN KEY (book_id) REFERENCES book(id),
      FOREIGN KEY (borrower_id) REFERENCES borrower(id)
    );

!SLIDE transition=fade

# The Author table in ScalaQuery

    import org.scalaquery.ql.basic.{BasicTable => Table}
    import org.scalaquery.ql.TypeMapper._
    import org.scalaquery.ql._

    object Author extends Table[
      (Int, String, String, Option[String], String, Option[String])
    ]("AUTHOR") {

      def id = column[Int]("id", O NotNull, O PrimaryKey)
      def firstName = column[String](
        "first_name", O NotNull, O DBType "varchar(50)"
      )
      def lastName = column[String](
        "last_name", O NotNull, O DBType "varchar(50)"
      )
      def middleName = column[Option[String]](
        "middle_name", O DBType "varchar(50)"
      )
      def nationality = column[String](
        "nationality", O Default "US", O DBType "varchar(100)"
      )
      def birthYear = column[Option[String]](
        "year_of_birth", O DBType "varchar(4)"
      )

      def * = id ~ firstName ~ lastName ~ middleName ~ nationality ~ birthYear
    }

!SLIDE transition=fade

# A little more detail

`first_name` is of type `String`, because it's not nullable:

    def firstName = column[String](
      "first_name", O NotNull, O DBType "varchar(50)"
    )

`middle_name` is `Option[String]`, to handle the null case:

    def middleName = column[Option[String]](
      "middle_name", O DBType "varchar(50)"
    )

`nationality` is of type `String`, because we've supplied a default, so
it'll always have a value:

    def nationality = column[String](
      "nationality", O Default "US", O DBType "varchar(100)"
    )

!SLIDE transition=fade

# Telling ScalaQuery which `defs` are columns

Since the columns are just normal Scala functions, you have to tell ScalaQuery
which functions map to table columns. That's what the `def *` does:

    // NOTE: SQLite tables are in upper case, and the driver doesn't upcase.
    object Author extends Table[(Int, String, Option[String])]("AUTHOR") {
      ...

      def * = id ~ firstName ~ lastName ~ middleName ~ nationality ~ birthYear
    }

!SLIDE transition=fade

# The Book table, with Foreign Keys

    object Book extends Table[(Int, String, Int, Option[Int])]("book") {
      def id = column[Int]("id", O NotNull, O PrimaryKey)
      def title = column[String](
        "title", O NotNull, O DBType "varchar(100)"
      )
      def authorID = column[Int]("author_id", O NotNull)
      def coAuthorID = column[Option[Int]]("co_author_id")
      def fkAuthor = foreignKey("fk_author_id", authorID, Author)(_.id)
      def fkCoAuthor = foreignKey("fk_coauthor_id", authorID, Author)(_.id)

      def * = id ~ title ~ authorID ~ coAuthorID 
    }

!SLIDE transition=fade

# A simple query

Queries are `for` comprehensions.

For instance, let's load the names of all authors from the US.

    import org.scalaquery.ql._
    import org.scalaquery.ql.extended.SQLiteDriver.Implicit._

    ...

    val nameQuery = for (a <- Author if a.nationality === "US")
      yield a.last_name ~ a.first_name

Queries are *lazy*: They are built outside of a `Session` and do not touch
the database until invoked.

    db withSession {
      val list: List[(String, String)] = nameQuery.list
    }

!SLIDE transition=fade

# A simple query: Complete example

A complete working program, with our simple query:

    import org.scalaquery.ql.extended.SQLiteDriver.Implicit._
    import org.scalaquery.session._
    import org.scalaquery.session.Database.threadLocalSession

    object ShowAuthors {
      def main(args: Array[String]) {
        val db = Database.forURL("jdbc:sqlite:testdb.sqlite3",
                                 driver = "org.sqlite.JDBC")
        val nameQuery = for {a <- Author if (a.nationality === "US")}
                          yield a.id ~ a.lastName ~ a.firstName
        db withSession {
          val list: List[(Int, String, String)] = nameQuery.list
          for ((id, last, first) <- list)
              println("%02d: %s, %s".format(id, last, first))
        }
      }
    }

Output, when run against against my test database:

    01: Sagan, Carl
    02: Odersky, Martin
    03: Spoon, Lex
    04: Venners, Bill


