# The Scala Query ORM

<div markdown="1" id="logo">
<img src="ardentex-logo.png"/>
</div>

**Brian Clapper**\
**ArdenTex, Inc.**\
*@brianclapper*\
*bmc@ardentex.com*

16 July, 2011

----------

# Beyond this point, there be dragons

* *WARNING!* I am *not* a ScalaQuery expert. To prepare this talk, I did
  what anyone else would do: Dug through the docs, Googled (a *lot*), and
  hacked code.
* From here on, incorrect information is my fault.
* ... unless I can blame the documentation.
* However, if you hurt yourself, or your machine, based on this presentation,
  it's *your* fault.
* ... unless you can blame the documentation.

----------

# The Scala Query ORM

* Written by Stefan Zeiger
* ScalaQuery is "an API / DSL built on top of JDBC".
* Provides compile-time checking and type-safety for queries
  * Database entities have static types.
* Uses relational algebra and query comprehensions
* Can be composed, the way one can compose Scala's collection classes.
* Does not rely on mutable state.
* Supports PostgreSQL, MySQL, H2, HSQLDB/HyperSQL, Derby/JavaDB,
  MS SQL Server, MS Access, and SQLite. Other RDBMs are supported, but
  with possibly reduced functionality.
  
----------

# Making a Connection
  
ScalaQuery requires an underlying JDBC connection.

All DB calls go through a `Session`, which is obtained from a `Database`,
using a standard JDBC URL.

```scala
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

val db = Database.forURL(
  "jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1",
  driver = "org.h2.Driver"
)
```

----------

# Using the Session

Implicitly:

```scala
val myQuery = ...
db withSession {
  myQuery.list()
}
```

Explicitly:

```scala
val myQuery = ...
db withSession { session =>
  myQuery.list()(session)
}
```

----------

# Schema, redux

Part of our schema (in SQLite-speak):

```sql
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
```

----------

# Mapping tables in ScalaQuery

For simple tables, use the `BasicTable` type:

```scala
import org.scalaquery.ql.basic.{BasicTable => Table}
```

For databases supporting extended features, use `ExtendedTable`:

```scala
import org.scalaquery.ql.extended.{ExtendedTable => Table}
```
    
We'll be using `ExtendedTable`, because auto-increment isn't supported
in `BasicTable`. However, `ExtendedTable` only works if there's a specific
ScalaQuery driver for the underlying database.

----------

# The Author table in ScalaQuery

```scala
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql._

object Author
extends Table[(Int, String, String, Option[String], String, Option[String])]("AUTHOR") {

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
```

----------

# A little more detail

`first_name` is of type `String`, because it's not nullable:

```scala
def firstName = column[String](
  "first_name", O NotNull, O DBType "varchar(50)"
)
```

`middle_name` is `Option[String]`, to handle the null case:

```scala
def middleName = column[Option[String]](
  "middle_name", O DBType "varchar(50)"
)
```

`nationality` is of type `String`, because we've supplied a default, so
it'll always have a value:

```scala
def nationality = column[String](
  "nationality", O Default "US", O DBType "varchar(100)"
)
```

----------

# A little more detail

Column options like `O NotNull` and `O Default` are used for creating tables
via ScalaQuery. They can be omitted, if you create tables another way.

```scala
def nationality = column[String](
  "nationality", O Default "US", O DBType "varchar(100)"
)
```

----------

# Telling ScalaQuery which methods correspond to columns

Since the columns are just normal Scala functions, you have to tell ScalaQuery
which functions map to table columns. That's what the `def *` does:

```scala
// NOTE: SQLite tables are in upper case, and the driver doesn't upcase.
object Author extends Table[(Int, String, Option[String])]("AUTHOR") {
  ...

  def * = id ~ firstName ~ lastName ~ middleName ~ nationality ~ birthYear
}
```

Mnemonic: "`def *`" is like "`SELECT *`"

----------

# The Book and BookAuthor tables

`Book` describes a book:

```scala
object Book extends Table[(Int, String)]("BOOK") {
  def id = column[Int]("id", O NotNull, O PrimaryKey, O AutoInc)
  def title = column[String](
    "title", O NotNull, O DBType "varchar(100)"
  )

  def * = id ~ title
}
```

`BookAuthor` is the intersection table that links authors to their books.
Note the foreign keys:

```scala
object BookAuthor extends Table[(Int, Int)]("BOOKAUTHOR") {
  def authorID = column[Int]("author_id", O NotNull)
  def bookID = column[Int]("book_id")

  def fkAuthorID = foreignKey("fk_author_id", authorID, Author)(_.id)
  def fkBookID = foreignKey("fk_book_id", bookID, Book)(_.id)

  def * = authorID ~ bookID
}
```


----------

# Queries

Queries are *for comprehensions*.

For instance, let's load the names of all authors from the US.

```scala
import org.scalaquery.ql._
import org.scalaquery.ql.extended.SQLiteDriver.Implicit._
...
val nameQuery =
  for (a <- Author if a.nationality === "US")
    yield a.lastName ~ a.firstName
```

The tilde construct (`a.lastName ~ a.firstName`) is a *column projection*.
Only those columns are returned.

----------

# Queries

There are also convenience functions for filtering. e.g.:

```scala
val nameQuery = Author.where(_.nationality === "US")
```

Queries are *lazy*: They are built outside of a `Session` and do not touch
the database until invoked.

```scala
db withSession {
  val list: List[(String, String)] = nameQuery.list
}
```

----------

# A simple query: Complete example

A complete working program, with the name query.

```scala
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
```

----------

# A simple query: Output

Output, when run against against my test database:

    $ scala -cp ... ShowAuthors US
    01: Sagan, Carl
    03: Spoon, Lex
    04: Venners, Bill

----------

# Implicit Joins

ScalaQuery's implicit joins are expressed with nested `for` comprehensions.

For instance, to load all authors who actually have books, along with their
books' titles, you might use this implicit join:

```scala
val q1 = for {a <- Author
              b <- Book
              ab <- BookAuthor if (ab.bookID === b.id) &&
                                  (ab.authorID === a.id)}
  yield a.lastName ~ a.firstName ~ b.title
```

----------

# Explicit Joins

ScalaQuery also supports explicit joins. In fact, you can't do an outer
join without using an explicit join.

Issues with explicit joins:

* Explicit multi-way joins are broken. See this May 27 thread:
  <http://goo.gl/76zNU>
* The syntax for explicit joins can quickly get complicated.
* Implicit joins are generally easier to read and construct.

----

# Explicit Joins

**Example**

Again, load all authors who actually have books, this time without the
titles (since that would require a multi-way explicit join, which is broken):

```scala
val q2 =
  for {Join(a, ab) <- Author innerJoin BookAuthor on (_.id is _.authorID)}
    yield a.lastName ~ a.firstName
```

Note that, in the above query, authors with multiple books will occur
multiple times in the result set.

----------

# COUNT, DISTINCT, GROUP BY

## COUNT, with GROUP BY

Count the number of authors for each book:

```scala
// SELECT COUNT(ba.author_id), b.title FROM book b, bookauthor ba
// WHERE b.id = ba.book_id GROUP BY b.title`

for {ba <- BookAuthor
     b  <- Book if (ba.bookID === b.id)
     _  <- Query groupBy ba.bookID}
  yield ba.authorID.count ~ b.title
```
      
(Personally, I find this syntax to be somewhat counterintuitive.)

----------

# COUNT, DISTINCT, GROUP BY

## DISTINCT

There doesn't appear to be a `SELECT DISTINCT` equivalent, though
there *is* a `COUNT(DISTINCT column)`:

```scala
for (ba <- BookAuthor) yield ba.author_id.countDistinct
```

----------

# Inserts

Use `insert` to insert a single row. If you use `insert` on the main
table object, you have to specify all columns, even `AutoInc` ones:
    
```scala
Author insert (10, "Vonnegut", "Kurt", None, "US", None)
```

You can also use column projections, to specify just some of the columns:

```scala
Author.lastName ~ Author.firstName insert ("Vonnegut", "Kurt") 
```

`insertAll` inserts multiple rows at once:

```scala
Author.lastName ~ Author.firstName insertAll(
  ("Vonnegut", "Kurt"),
  ("Twain", "Mark")
)
```

----------

# Updates

When a query returns a projection of columns, you can use it to update
the rows selected by the query:

```scala
object UpdateAuthor {
  val DbUrl = "jdbc:sqlite:testdb.sqlite3"
  def run(lastNameToFind: String, newNationality: String) = {
      val db = Database.forURL(DbUrl, driver = "org.sqlite.JDBC")
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
```

----------

# Updates: Example run

Example run:

    scala> UpdateAuthor.run("Vonnegut", "US")
    *** BEFORE:
    Kurt Vonnegut: CA
    Mrs. Vonnegut: CA
    *** AFTER:
    Kurt Vonnegut: US
    Mrs. Vonnegut: US
    
----------

# Parameterized Queries

## (or, Keeping Little Bobby Tables at bay)

<div style="width: 100%; text-align: center">
<img src="exploits_of_a_mom.png" class="illustration" note="final slash needed"/>
</div>

----------

# Parameterized Queries

ScalaQuery uses *query templates* for parameterized queries. For example:

```scala
object ShowAuthorsByNameAndCountry {
  def run(lastName: String, nationality: String) = {
    // Create a parameterized query template with one parameter.
    val qt = for {lnc ~ cc <- Parameters[String, String]
                  a <- Author if a.lastName === lnc && a.nationality === cc}
                 yield a.id ~ a.lastName ~ a.firstName ~ a.nationality
    
    // Instantiate and run.
    db withSession {
      for (rs <- qt((lastName, countryCode)))
        printf("%02d: %s %s (%s)\n", rs._1, rs._3, rs._2, rs._4)
    }
}
```
   
----------

# Examples of some complex statements

Using a lifted function to group by day of week:

```scala
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
```
   
----------

# Examples of some complex statements

Deleting all users with a specific email address:

```scala
var emailToDelete = ...
var q = for {u <- Users if u.email === emailToDelete} yield u.id 
users.foreach(u => {
  UserTopics.where(_.user_id === u).delete
})
```

----------

# Miscellaneous

## Creating tables

ScalaQuery can create tables:

```scala
db withSession {
  (Author.ddl ++ Book.ddl ++ BookAuthor.ddl) create
}
```

## Printing a generated query

```scala
val q = for (a <- Author if a.nationality === "US")
  yield a.last_name ~ a.first_name
println(q.selectStatement)
```

----------

# My Impressions

## Pros

* Powerful
* Strong type safety
* Highly composable

----------

# My Impressions

## Cons

* Potentially less readable than, say, Squeryl
* Documentation is poor
    + No documentation on *scalaquery.org*
    + Minimal docs on official GitHub wiki
    + Additional docs scattered throughout Stefan Zeiger's blog.
    + The mailing list archives sometimes help
    + Prepare to spend time Googling and poring through the source code.

----------

# My Impressions

* Would I use it? Sure.
* Would it be my first choice? I'll let you know, after I see everyone
  else's presentations.

----------

# Additional info

* This presentation and some sample code can be found at
  <https://github.com/bmc/phase-scala-orms/tree/master/ScalaQuery>
* ScalaQuery's GitHub repo: <https://github.com/szeiger/scala-query>
* ScalaQuery's web site: <http://scalaquery.org/>
* The Google Groups group: <http://groups.google.com/group/scalaquery>
* "scalaquery" tag on StackOverflow:
  <http://stackoverflow.com/questions/tagged/scalaquery> (not much there)
