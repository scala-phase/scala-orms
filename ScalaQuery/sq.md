!SLIDE

# The Scala Query ORM

Brian Clapper, *bmc@ardentex.com*

ArdenTex, Inc.

16 July, 2011

!SLIDE

# The Scala Query ORM

From *scalaquery.org*:

- ScalaQuery is "an API / DSL built on top of JDBC".
- Provides compile-time checking and type-safety for queries
    * Database entities have static types.
- Composable, non-leaky abstractions
    * Relational algebra and query comprehensions
    * Can be composed, the way one can compose Scala's collection classes.
- Does not rely on mutable state.
- Supports PostgreSQL, MySQL, H2, HSQLDB/HyperSQL, Derby/JavaDB,
  MS SQL Server, MS Access, and SQLite.
  
!SLIDE

# Making a Connection
  
ScalaQuery requires an underlying JDBC connection.

All DB calls go through a `Session`, which is obtained from a `Database`:

    import org.scalaquery.session._
    import org.scalaquery.session.Database.threadLocalSession

    val myQuery = ...
    val db = Database.forURL(
      "jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1",
      driver = "org.h2.Driver"
    )
    db withSession {
      myQuery.list()
    }

