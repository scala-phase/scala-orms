!SLIDE title-page

# Chariot Solutions

## The Querulous ORM

Jamie Allen, *jallen@chariotsolutions.com*, @jamie_allen

*16 July, 2011*

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

# Design Patterns of Modularity

* Must have very few assumptions hard-coded
* No concrete types!
* Ruby's open classes, method aliasing and even metaclasses do not solve this problem

* Dependency Injection
* Factories
* Decorators

* Querulous achieves modularity by providing an "injection point" for the programmer to layer on custom functionality.

* "Why I Love Everything You Hate About Java"

!SLIDE transition=fade

# Querulous is MySQL-Specific!

* There are several generic JDBC forks on GitHub, including:
** Rose Toomey, forked from
** Brendan McAdams, forked from
** Rhys Keepence

* I'm using Rose's querulous-generic fork for the purposes of this talk, and SQLite as the database.

!SLIDE transition=fade

# Making a Connection
  
!SLIDE transition=fade

# Query

!SLIDE transition=fade

# Insert

!SLIDE transition=fade

# Update

!SLIDE transition=fade

# Delete

!SLIDE transition=fade

# My Impressions

## Pros


## Cons

!SLIDE smbullets incremental transition=fade

# My Impressions (cont'd)

!SLIDE transition=fade

# Additional info

* This presentation and some sample code can be found at
  <https://github.com/scala-phase/scala-orms/tree/master/Querulous>
* Querulous's GitHub repo: <https://github.com/twitter/querulous>
* Rose's querulous-generic GitHub repo: <https://github.com/novus/querulous-generic>
* Twitter Blog: Why I Love Everything You Hate About Java: <http://magicscalingsprinkles.wordpress.com/2010/02/08/why-i-love-everything-you-hate-about-java/>
* No Google Groups group
* No tag on StackOverflow

*Like Brian, I created this presentation with Scott Chacon's ShowOff tool. See
<https://github.com/schacon/showoff>.*
