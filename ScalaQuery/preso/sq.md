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

    @@@ scala
    import org.scalaquery.session._
    import org.scalaquery.session.Database.threadLocalSession

    val db = Database.forURL(
      "jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1",
      driver = "org.h2.Driver"
    )

!SLIDE transition=fade

# Using the Session

Implicitly:

    @@@ scala
    val myQuery = ...
    db withSession {
      myQuery.list()
    }
    
Explicitly:

    @@@ scala
    val myQuery = ...
    db withSession { session =>
      myQuery.list()(session)
    }

!SLIDE transition=toss

# Tables: Schema, redux

Our schema, again:

    CREATE TABLE author (
      id             BIGINT AUTO_INCREMENT PRIMARY KEY,
      last_name      VARCHAR(50) NOT NULL,
      first_name     VARCHAR(50) NOT NULL,
      middle_name    VARCHAR(50) NULL,
      nationality    VARCHAR(100),
      year_of_birth  VARCHAR(4),
    )

    CREATE TABLE book (
      id           BIGINT AUTO_INCREMENT PRIMARY KEY,
      title        VARCHAR(100) NOT NULL,
      author_id    BIGINT NOT NULL,
      co_author_id BIGINT,

      FOREIGN KEY (author_id) REFERENCES author(id),
      FOREIGN KEY (co_author_id) REFERENCES author(id)
    )

!SLIDE transition=fade

# Tables: Schema, redux

    CREATE TABLE borrower (
      id        BIGINT AUTO_INCREMENT PRIMARY KEY,
      phone_num VARCHAR(20) NOT NULL,
      address   TEXT NOT NULL
    )

    CREATE TABLE borrowal (
      id BIGINT                AUTO_INCREMENT PRIMARY KEY,
      book_id                  BIGINT NOT NULL,
      borrower_id              BIGINT NOT NULL,
      scheduled_to_return_on   DATE NOT NULL,
      returned_on              TIMESTAMP,
      num_nonreturn_phonecalls INT,

      FOREIGN KEY (book_id) REFERENCES book(id),
      FOREIGN KEY (borrower_id) REFERENCES borrower(id)
    )

!SLIDE transition=fade

# The Author table in ScalaQuery

    @@@ scala
    object Author extends Table[
      (Int, String, String, Option[String], String, Option[String])
    ]("author") {

      // id BIGINT AUTO_INCREMENT PRIMARY KEY
      def id = column[Int]("id", O AutoInc, O NotNull, O PrimaryKey)
      // last_name VARCHAR(50) NOT NULL,
      def firstName = column[String](
        "first_name", O NotNull, O DBType "varchar(50)"
      )
      // last_name VARCHAR(50) NOT NULL,
      def lastName = column[String](
        "last_name", O NotNull, O DBType "varchar(50)"
      )
      // middle_name VARCHAR(50) NULL,
      def middleName = column[Option[String]](
        "middle_name", O DBType "varchar(50)"x
      )
      // nationality VARCHAR(100)
      def nationality = column[String](
        "nationality", O Default "US", O DBType "varchar(100)"
      )
      // year_of_birth VARCHAR(4)
      def birthYear = column[Option[String]](
        "year_of_birth", O DBType "varchar(4)"
      )

      def * = id ~ nationality ~ birth_year
    }

!SLIDE transition=fade

# A little more detail

`first_name` is of type `String`, because it's not nullable:

    @@@ scala
    def firstName = column[String](
      "first_name", O NotNull, O DBType "varchar(50)"
    )

`middle_name` is `Option[String]`, to handle the null case:

    @@@ scala
    // middle_name VARCHAR(50) NULL,
    def middleName = column[Option[String]](
      "middle_name", O DBType "varchar(50)"
    )

`nationality` is of type `String`, because we've supplied a default, so
it'll always have a value:

    @@@ scala
    def nationality = column[String](
      "nationality", O Default "US", O DBType "varchar(100)"
    )

!SLIDE transition=fade

# Telling ScalaQuery which `defs` are columns

Since the columns are just normal Scala functions, you have to tell ScalaQuery
which functions map to table columns. That's what the `def *` does:

    @@@ scala
    object Author extends Table[(Int, String, Option[String])]("author") {
      ...

      def * = id ~ nationality ~ birth_year
    }

!SLIDE transition=fade

# The Book table, with Foreign Keys

    @@@ scala
    object Book extends Table[(Int, String, Int, Option[Int])]("book") {
      // id BIGINT AUTO_INCREMENT PRIMARY KEY
      def id = column[Int]("id", O AutoInc, O NotNull, O PrimaryKey)

      // title VARCHAR(100) NOT NULL
      def title = column[String](
        "title", O NotNull, O DBType "varchar(100)"
      )

      // author_id BIGINT NOT NULL,
      def authorID = column[Int]("author_id", O NotNull)

      // co_author_id BIGINT NOT NULL,
      def coAuthorID = column[Option[Int]]("co_author_id")

      // FOREIGN KEY (author_id) REFERENCES author(id)
      def fkAuthor = foreignKey("fk_author_id", authorID, Author)(_.id)

      // FOREIGN KEY (author_id) REFERENCES author(id)
      def fkCoAuthor = foreignKey("fk_coauthor_id", authorID, Author)(_.id)
    }

!SLIDE incremental transition=blindX

# A simple query

Queries are `for` comprehensions.

For instance, let's load the names of all authors from the US.

    @@@ scala
    val query = for (a <- Authors if a.nationality === "US") yield
      a <- Authors
      b <- Books if (a.id is b.authorID)
