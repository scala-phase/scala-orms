This directory contains source for the [ScalaQuery][] portion of the
[PHASE][] talk on [Scala][] ORMs (June 16, 2011).

The slides are written in Markdown and are intended to be run through
(i.e., served by) [ShowOff][].

To run the slideshow, you need the following bits:

* A [Ruby][] installation. 1.8.7 will do fine.
* [RubyGems][].
* The [Rake][] gem.
* The [ShowOff][] gem. Installing ShowOff installs its dependencies.
* The [Sass][] gem. (I maintain the CSS files as SASS files. It makes Life
  with CSS a lot more tolerable.)
* The Mongrel web server (optional; ShowOff will use Webrick, by default).
  Install via `gem install mongrel`.
  
Once you have accumulated all that baggage, you can build and run the
slideshow with:

    $ rake run

[ShowOff][] starts a web server listening on port 9090. Surfing to that port,

[ScalaQuery]: http://scalaquery.org/
[ShowOff]: https://github.com/schacon/showoff
[PHASE]: http://www.meetup.com/scala-phase
[Scala]: http://www.scala-lang.org/
[Ruby]: http://www.ruby-lang.org/
[RubyGems]: http://rubygems.org/
[Sass]: http://sass-lang.com/
[Rake]: http://rake.rubyforge.org/
