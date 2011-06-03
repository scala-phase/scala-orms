This directory contains source for the [ScalaQuery][] portion of the
[PHASE][] talk on [Scala][] ORMs (June 16, 2011).

The slides are written in Markdown and translated to [S5][] HTML+Javascript
format via John MacFarlane's excellent [Pandoc][] tool.

To create the slideshow, you need the following bits:

* A [Ruby][] installation. 1.8.7 will do fine.
* [RubyGems][].
* The [Rake][] gem.
* The [Sass][] gem. (I maintain the CSS files as SASS files. It makes Life
  with CSS a lot more tolerable.)
* [Pandoc][] (This may require installing Haskell, unless you can find a
  decent binary. Some Linux distros have pandoc packages.)
  
Once you have accumulated all that baggage, you can build the slideshow with:

    $ rake

The slides will end up in `sd.html`. If you decide to copy the slideshow
somewhere else, the stuff under the `ui` directory is also required.

[ScalaQuery]: http://scalaquery.org/
[PHASE]: http://www.meetup.com/scala-phase
[Scala]: http://www.scala-lang.org/
[Pandoc]: http://johnmacfarlane.net/pandoc/
[S5]: http://meyerweb.com/eric/tools/s5/
[Ruby]: http://www.ruby-lang.org/
[RubyGems]: http://rubygems.org/
[Sass]: http://sass-lang.com/
[Rake]: http://rake.rubyforge.org/
