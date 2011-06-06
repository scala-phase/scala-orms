!SLIDE title-page

# The Scala Query ORM

Brian Clapper, *bmc@ardentex.com*

ArdenTex, Inc.

*16 July, 2011*

!SLIDE smbullets incremental transition=fade

# The Scala Query ORM

- Written by Stefan Zeiger
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

All DB calls go through a `Session`, which is obtained from a `Database`,
using a standard JDBC URL.

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

# Schema, redux

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
      title        VARCHAR(100) NOT NULL
    );

    CREATE TABLE bookauthor (
      book_id   BIGINT NOT NULL,
      author_id BIGINT NOT NULL,

      PRIMARY KEY (book_id, author_id),
      FOREIGN KEY (author_id) REFERENCES author(id),
      FOREIGN KEY (book_id) REFERENCES book(id)
    );  

!SLIDE transition=fade

# Schema, redux

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

# Tables in ScalaQuery

For simple tables, use the `BasicTable` type:

    import org.scalaquery.ql.basic.{BasicTable => Table}

For databases supported extended features, use `ExtendedTable`:

    import org.scalaquery.ql.extended.{ExtendedTable => Table}
    
We'll be using `ExtendedTable`, because auto-increment isn't supported
in `BasicTable`. However, `ExtendedTable` only works if there's a specific
ScalaQuery driver for the underlying database.

!SLIDE transition=fade

# The Author table in ScalaQuery

    import org.scalaquery.ql.extended.{ExtendedTable => Table}
    import org.scalaquery.ql.TypeMapper._
    import org.scalaquery.ql._

    object Author extends Table[
      (Int, String, String, Option[String], String, Option[String])
    ]("AUTHOR") {

      def id = column[Int]("id", O NotNull, O PrimaryKey, O AutoInc)
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


Column options like `O NotNull` and `O Default` are used for creating tables
via ScalaQuery. They can be omitted, if you create tables another way.

!SLIDE transition=fade

# Telling ScalaQuery which methods correspond to columns

Since the columns are just normal Scala functions, you have to tell ScalaQuery
which functions map to table columns. That's what the `def *` does:

    // NOTE: SQLite tables are in upper case, and the driver doesn't upcase.
    object Author extends Table[(Int, String, Option[String])]("AUTHOR") {
      ...

      def * = id ~ firstName ~ lastName ~ middleName ~ nationality ~ birthYear
    }

Mnemonic: "`def *`" is like "`SELECT *`"

!SLIDE transition=fade

# The Book and BookAuthor tables

`Book` describes a book:

    object Book extends Table[(Int, String)]("BOOK") {
      def id = column[Int]("id", O NotNull, O PrimaryKey, O AutoInc)
      def title = column[String](
        "title", O NotNull, O DBType "varchar(100)"
      )

      def * = id ~ title
    }

`BookAuthor` is the intersection table that links authors to their books.
Note the foreign keys:

    object BookAuthor extends Table[(Int, Int)]("BOOKAUTHOR") {
      def authorID = column[Int]("author_id", O NotNull)
      def bookID = column[Int]("book_id")

      def fkAuthorID = foreignKey("fk_author_id", authorID, Author)(_.id)
      def fkBookID = foreignKey("fk_book_id", bookID, Book)(_.id)

      def * = authorID ~ bookID
    }


!SLIDE transition=fade

# A simple query

Queries are *for comprehensions*.

For instance, let's load the names of all authors from the US.

    import org.scalaquery.ql._
    import org.scalaquery.ql.extended.SQLiteDriver.Implicit._

    ...

    val nameQuery = for (a <- Author if a.nationality === "US")
      yield a.last_name ~ a.first_name

There are also convenience functions for filtering. e.g.:

    val nameQuery = Author.where(_.nationality === "US")

Queries are *lazy*: They are built outside of a `Session` and do not touch
the database until invoked.

    db withSession {
      val list: List[(String, String)] = nameQuery.list
    }

!SLIDE transition=fade

# A simple query: Complete example

A complete working program, with our simple query:

    import org.scalaquery.ql.basic.BasicDriver.Implicit._
    import org.scalaquery.session._
    import org.scalaquery.session.Database.threadLocalSession

    object ShowAuthors {
      def main(args: Array[String]) {
        val db = Database.forURL("jdbc:sqlite:testdb.sqlite3",
                                 driver = "org.sqlite.JDBC")
        var nationality = if (args.length == 0) "US" else args(0)
        val nameQuery = for {a <- Author if (a.nationality === nationality)}
                          yield a.id ~ a.lastName ~ a.firstName
        db withSession {
          val list: List[(Int, String, String)] = nameQuery.list
          for ((id, last, first) <- list)
              printf("%02d: %s, %s\n", id, last, first)
        }
      }
    }

Output, when run against against my test database:

    $ scala -cp ... ShowAuthors US
    01: Sagan, Carl
    03: Spoon, Lex
    04: Venners, Bill

!SLIDE transition=fade

# Implicit Joins

ScalaQuery's implicit joins are expressed with nested `for` comprehensions.

For instance, to load all authors who actually have books, along with their
books' titles, you might use this implicit join:

    val q1 = for {a <- Author
                  b <- Book
                  ab <- BookAuthor if (ab.bookID === b.id) &&
                                      (ab.authorID === a.id)}
               yield a.lastName ~ a.firstName ~ b.title

!SLIDE transition=fade

# Explicit Joins

ScalaQuery also supports explicit joins. In fact, you can't do an outer
join without using an explicit join.

Issues with explicit joins:

* Explicit multi-way joins are broken. See this May 27 thread:
  <https://groups.google.com/forum/#!topic/scalaquery/lgI1ADShEM8>.
  "You'd have to do them pair-wise, using sub-selects as necessary."
* The syntax for explicit joins can quickly get complicated.
* Implicit joins are generally easier to read and construct.

## Example

Again, load all authors who actually have books, this time without the
titles (since that would require a multi-way explicit join, which is broken):

    val q2 =
      for {Join(a, ab) <- Author innerJoin BookAuthor on (_.id is _.authorID)}
        yield a.lastName ~ a.firstName

Note that, in the above, authors with multiple books will occur multiple
times in the result set.

!SLIDE transition=fade

# COUNT, DISTINCT, GROUP BY

## COUNT, with GROUP BY

Count the number of authors for each book:

    // SELECT COUNT(ba.author_id), b.title FROM book b, bookauthor ba
    // WHERE b.id = ba.book_id GROUP BY b.title`

    for {ba <- BookAuthor
         b  <- Book if (ba.bookID === b.id)
         _  <- Query groupBy ba.bookID}

      yield ba.authorID.count ~ b.title
      
(Personally, I find this syntax to be somewhat counterintuitive.)

## DISTINCT

There doesn't appear to be a `SELECT DISTINCT` equivalent, though
there *is* a `COUNT(DISTINCT column)`:

    for (ba <- BookAuthor) yield ba.author_id.countDistinct

!SLIDE transition=fade

# Inserts

Use `insert` to insert a single row. If you use `insert` on the main
table object, you have to specify all columns, even `AutoInc` ones:
    
    Author insert (10, "Vonnegut", "Kurt", None, "US", None)

You can also use column projections, to specify just some of the columns:

    Author.lastName ~ Author.firstName insert ("Vonnegut", "Kurt") 

`insertAll` inserts multiple rows at once:

    Author.lastName ~ Author.firstName insertAll(
      ("Vonnegut", "Kurt"),
      ("Twain", "Mark")
    )

!SLIDE transition=fade

# Updates

When a query returns a projection of columns, you can use it to update
the rows selected by the query:

    object UpdateAuthor {
      def run(lastNameToFind: String, newNationality: String) = {
          val db = Database.forURL("jdbc:sqlite:testdb.sqlite3",
                                   driver = "org.sqlite.JDBC")

          db withSession {
            show("*** BEFORE:", lastNameToFind)

            val q2 = for {a <- Author if a.lastName === lastNameToFind}
                       yield a.nationality
            q2.update(newNationality)

            show("*** AFTER:", lastNameToFind)
        }
      }

      def show(prefix: String, lastName: String) = {
        val q1 = for {a <- Author if a.lastName === lastName}
                   yield a.firstName ~ a.lastName ~ a.nationality
        println(prefix)
        for (rs <- q1.list)
          printf("%s %s: %s\n", rs._1, rs._2, rs._3)
      }
    }

!SLIDE transition=fade

# Updates: Example run

Example run:

    scala> UpdateAuthor.run("Vonnegut", "US")
    *** BEFORE:
    Kurt Vonnegut: CA
    Mrs. Vonnegut: CA
    *** AFTER:
    Kurt Vonnegut: US
    Mrs. Vonnegut: US
    
!SLIDE transition=fade

# Parameterized Queries (Keeping Little Bobby Tables at bay)

<img src="exploits_of_a_mom.png" class="illustration" markdown="1"/>

ScalaQuery uses *query templates* for parameterized queries. For example:

    object ShowAuthorsByNameAndCountry {
      def run(lastName: String, nationality: String) = {
        // Create a parameterized query template with one parameter.
        val qt = for {lnc ~ cc  <-  Parameters[String, String]
                      a <- Author if a.lastName === lnc && a.nationality === cc}
                     yield a.id ~ a.lastName ~ a.firstName ~ a.nationality
        
        // Instantiate and run.
        db withSession {
          for (rs <- qt((lastName, countryCode)))
            printf("%02d: %s %s (%s)\n", rs._1, rs._3, rs._2, rs._4)
        }
    }
       
!SLIDE transition=fade

# Examples of some complex statements

Using a lifted function to group by day of week

    val SalesPerDay = new Table[(Date, Int)]("SALES_PER_DAY") {
      def day = column[Date]("DAY", O.PrimaryKey)
      def count = column[Int]("COUNT")
      def * = day ~ count
    }

    val q = for {
        dow ~ count <- SalesPerDay.map(s => dayOfWeek2(s.day) ~ s.count)
        _ <- Query groupBy dow
      } yield dow ~ count.sum.get

    q.foreach { case (dow, sum) => println(" " + dow + " -> " + sum) }

Delete all users with a specific email address

    var emailToDelete = ...
    var q = for {u <- Users if u.email === emailToDelete} yield u.id 
    users.foreach(u => {
      UserTopics.where(_.user_id === u).delete
    })

!SLIDE transition=fade

# Miscellaneous

## Creating tables

ScalaQuery can create tables:

    db withSession {
        (Author.ddl ++ Book.ddl ++ BookAuthor.ddl) create
    }

## Printing a generated query

    val q = for (a <- Author if a.nationality === "US")
      yield a.last_name ~ a.first_name
    println(q.selectStatement)

!SLIDE transition=fade

# ScalaQuery Impressions

## Pros

* Powerful
* Strong type safety
* Monadic
* Highly composable

## Cons

* Potentially less readable than, say, Squeryl
* Documentation is poor
  * No documentation on *scalaquery.org*
  * Minimal docs on official wiki
  * Additional docs scattered throughout Stefan Zeiger's blog.
  * The mailing list archives can help.
  * You end up spending a lot of time googling and poring through
    the ScalaQuery source code.

!SLIDE transition=fade

# Additional info

* This presentation and some sample code can be found at
  <https://github.com/bmc/phase-scala-orms/tree/master/ScalaQuery>
* ScalaQuery's GitHub repo: <https://github.com/szeiger/scala-query>
* ScalaQuery's web site: <http://scalaquery.org/>
* The Google Groups group: <http://groups.google.com/group/scalaquery>
* "scalaquery" tag on StackOverflow:
  <http://stackoverflow.com/questions/tagged/scalaquery> (not much there)
