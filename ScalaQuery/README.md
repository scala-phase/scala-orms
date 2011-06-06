This directory contains source for the [ScalaQuery][] portion of the
[PHASE][] talk on [Scala][] ORMs (June 16, 2011).

# Slides

The slides are written in Markdown and are intended to be run through
(i.e., served by) [ShowOff][].

## Running the slide show from source

To run the slideshow, you need the following bits:

* A [Ruby][] installation. 1.8.7 will do fine.
* [RubyGems][].
* The [Rake][] gem.
* The [ShowOff][] gem. Installing ShowOff installs its dependencies.
* The [Sass][] gem. (I maintain the CSS files as Sass files. It makes Life
  with CSS a lot more tolerable.)

These optional bits can also come in handy:

* [PDFKit][], for generating PDF handouts.
* [RMagick][], which allows ShowOff to resize images.
* The Mongrel web server. (ShowOff will use Webrick, by default.)
  Install via `gem install mongrel`.
  
Once you have accumulated all that baggage, you can build and run the
slideshow with:

    $ rake run

You can also run the slideshow like this:

    $ showoff serve

ShowOff regenerates edited slides automatically, so you don't need to
restart ShowOff if you're just editing the slides. Reloading the browser
page is sufficient to display the changes. However, if you're modifying the
Sass CSS sources, ShowOff won't know to regenerate the corresponding CSS
files, so you have to kill ShowOff, rebuild the CSS files, and restart.
`rake run` handles rebuilding everything that needs to be rebuilt, before
starting ShowOff.

[ShowOff][] starts a web server listening on port 9090. Surf to that port,
to run the slide show.

## Static Slideshow

For convenience (and so you don't have to load up all that Ruby stuff),
a static, pre-generated version of the slide show is in the `static` directory.

# Sample Code

The `code` subdirectory contains an [SBT][] project with some sample code
that demonstrates the ScalaQuery concepts discussed in this talk. All code
was tested against a locally built 0.9.5-SNAPSHOT version of ScalaQuery,
compiled against Scala 2.8.1.

[ScalaQuery]: http://scalaquery.org/
[ShowOff]: https://github.com/schacon/showoff
[PHASE]: http://www.meetup.com/scala-phase
[Scala]: http://www.scala-lang.org/
[Ruby]: http://www.ruby-lang.org/
[RubyGems]: http://rubygems.org/
[Sass]: http://sass-lang.com/
[Rake]: http://rake.rubyforge.org/
[PDFKit]: https://github.com/jdpace/PDFKit
[RMagick]: http://rmagick.rubyforge.org/
[SBT]: http://code.google.com/p/simple-build-tool
