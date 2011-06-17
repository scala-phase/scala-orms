!SLIDE title-page

## The Querulous ORM

Jamie Allen, *jallen@chariotsolutions.com*, @jamie_allen

*July 16, 2011*

!SLIDE bullets incremental transition=fade

* Just like Brian, I am *not* a Querulous expert, and have never used it in anger.

!SLIDE smbullets incremental transition=fade

# Querulous

* Written by Matt Freels, Nick Kallen, Robey Pointer, Utkarsh Srivastava and Ed Caesar at Twitter
* Exposes SQL directly.  No DSL.  Sits on top of JDBC.  Can call stored procs.
* Designed primarily to meet the extreme demands of FlockDB (Twitter's distributed, fault-tolerant graph database), which demands very low-latency (sub-millisecond) response times for individual queries.
* A very minimalist database querying library.  Any excessive indirection would be intolerable in this environment.
* Designed for querying databases at low-latency, massive scale and with easy operability.
  
!SLIDE transition=fade

# Major Features

* Flexible timeouts
* Extensive logging
* Rich statistics
* Extremely modular, to support DIFFERENT timeout and health check strategies based on context
* As a result, it is extremely configurable

!SLIDE transition=fade

# Nick's Design Patterns of Modularity

* Must have very few assumptions hard-coded
* No concrete types!
* Ruby's open classes, method aliasing and even metaclasses do not solve this problem

* Dependency Injection
* Factories
* Decorators

* Querulous achieves modularity by providing an "injection point" for the programmer to layer on custom functionality.

* "Why I Love Everything You Hate About Java"

!SLIDE transition=fade

# Querulous is MySQL-Specific

* FlockDB uses MySQL as the storage engine

* There are several generic JDBC forks on GitHub, including:
** Rose Toomey, forked from
** Brendan McAdams, forked from
** Rhys Keepence

* I'm using Rose's querulous-generic fork for the purposes of this talk, and SQLite as the database.  Olle Kullberg has another fork as well.  
* Sean Rhea has a project called scalaqlite if you want to talk directly to SQLite without Querulous.

!SLIDE transition=fade

# Making a Connection
  
!SLIDE transition=fade

# Queries

* All queries go through a QueryEvaluator instance

  import com.twitter.querulous.evaluator.QueryEvaluator

  trait TestConnection {
    val queryEvaluator = QueryEvaluator("org.sqlite.JDBC", "jdbc:sqlite:phase", "", "")
  }

!SLIDE transition=fade

* Basic Query Semantics - YOU write the SQL

  case class Author(id: Int, firstName: String, lastName: String)
  object ShowAuthors extends TestConnection {
    def main(args: Array[String]) {
      getAuthors
    }

    def getAuthors() {
      val users = queryEvaluator.select(
	      "SELECT * FROM author WHERE id IN (?) OR first_name = ?", List(1,2), "Bill") { 
		      row =>
			      new Author(row.getInt("id"), 
			          row.getString("first_name"), 
			          row.getString("last_name"))
      }

	  users.map(author => println(author))
  	}
  }

!SLIDE transition=fade

# Transaction Support

  queryEvaluator.transaction { transaction =>
    transaction.select("SELECT ... FOR UPDATE", ...)
    transaction.execute("INSERT INTO author VALUES (?, ?, ?, ?, ?, ?)", 5, "Brendan", "McAdams", "Michelangelo", "US", "1980")
    transaction.execute("INSERT INTO author VALUES (?, ?, ?, ?, ?, ?)", 6, "Debasish", "Ghosh", null, "India", "1980")
  }

* Transactions will be rolled back in the case of an exception

!SLIDE transition=fade

#Composable Decorators

* Advanced features can be layered on using composing QueryFactory types

  val queryFactory = new RetryingQueryFactory(
                            new TimingOutQueryFactory(new SqlQueryFactory, 3000.millis), 5)

  val queryFactory = new DebuggingQueryFactory(
                            new RetryingQueryFactory(
	                                new TimingOutQueryFactory(new SqlQueryFactory, 3.seconds), 5), println)
!SLIDE transition=fade

# Statistics
* Use factories to get database stats

  val stats = new StatsCollector {
    def incr(name: String, count: Int) = Stats.incr(name, count)
    def time[A](name: String)(f: => A): A = Stats.time(name)(f)
  }
  val databaseFactory = new StatsCollectingDatabaseFactory(
                                 new ApachePoolingDatabaseFactory(...), stats)

* To memoize DB connections and maintain connection limits (if you are dynamically connecting to dozens of hosts)

  val databaseFactory = new MemoizingDatabaseFactory(new ApachePoolingDatabaseFactory(...))

!SLIDE transition=fade

# Async API

* Based on Twitter's com.twitter.util.Future, a non-Actor re-implementation of Scala Futures
* Uses the AsyncQueryEvaluator rather than the standard QueryEvaluator
* Methods immediately return values wrapped in a Future
* Blocking JDBC calls are executed within a thread pool

!SLIDE transition=fade

# Async API Example

  // returns Future[Seq[User]]
  val future = queryEvaluator.select(
                 "SELECT * FROM users WHERE id IN (?) OR name = ?", List(1,2,3), "Jacques") 
                   { row => new User(row.getInt("id"), row.getString("name"))}

  // Futures support a functional, monadic interface:
  val tweetsFuture = future flatMap { users =>
                       queryEvaluator.select("SELECT * FROM tweets WHERE user_id IN (?)", 
                            users.map(_.id)) { 
	                            row => new Tweet(row.getInt("id"), row.getString("text"))}
  }

  // futures only block when unwrapped.
  val tweets = tweetsFuture.apply()

!SLIDE transition=fade

# External Configuration
* AutoDisabling due to failure
* Database pooling
* Retries
* Timeouts

!SLIDE transition=fade

# My Impressions
## Pros
* Modularity is a plus
* Some very nice composable decorators for standard behavior (timeouts, retrying, debug, etc)
* Definitely has been tested under load
* Futures support looks very cool, async is hot

## Cons
* Limited support for Maven
* No support from Twitter for any database other than MySQL
* If you can't write SQL, you better learn

!SLIDE transition=fade

# Additional info

* This presentation and some sample code can be found at
  <https://github.com/scala-phase/scala-orms/tree/master/Querulous>
* Querulous's GitHub repo: <https://github.com/twitter/querulous>
* Rose's querulous-generic GitHub repo: <https://github.com/novus/querulous-generic>
* Sean Rhea's scalaqlite: <https://github.com/srhea/scalaqlite>
* Twitter Blog: Why I Love Everything You Hate About Java: <http://magicscalingsprinkles.wordpress.com/2010/02/08/why-i-love-everything-you-hate-about-java/>
* No Google Groups group
* No tag on StackOverflow

*Like Brian, I created this presentation with Scott Chacon's ShowOff tool. See
<https://github.com/schacon/showoff>.*
