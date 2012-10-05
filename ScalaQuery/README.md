This directory contains source for the [ScalaQuery][] portion of the
[PHASE][] talk on [Scala][] ORMs (June 16, 2011).

# Slides

The slides are written in Markdown and are intended to be run through
[Pandoc][], via the Rakefile.

Requires the following software:

* [Ruby][]
* The [Rake][] build tool
* John MacFarlane's excellent [Pandoc][] tool
* The `lessc` command, to process the [LESS][] stylesheets.

Once you have all that stuff handy, simply run:

    $ rake

to build `slides.html`, a fully self-contained [Slidy][] slide show. Just
open the file in your browser, and away you go.

You'll find the source for the slides in `slides.md`, a Markdown file.
For more information on using [Pandoc][] for slide generation, see
<http://johnmacfarlane.net/pandoc/README.html#producing-slide-shows-with-pandoc>.

You can view a pre-built version of this presentation at
<http://www.ardentex.com/publications/the-play-framework/>.

# Sample Code

The `code` subdirectory contains an [SBT][] project with some sample code
that demonstrates the ScalaQuery concepts discussed in this talk. All code
was tested against a locally built 0.9.5-SNAPSHOT version of ScalaQuery,
compiled against Scala 2.8.1.

[ScalaQuery]: http://scalaquery.org/
[PHASE]: http://www.meetup.com/scala-phase
[SBT]: http://code.google.com/p/simple-build-tool
[Ruby]: http://www.ruby-lang.org/
[Rake]: http://rake.rubyforge.org/
[Bundler]: http://gembundler.com/
[LESS]: http://lesscss.org/
[Pandoc]: http://johnmacfarlane.net/pandoc/
[PHASE]: http://scala-phase.org/
[Slidy]: http://www.w3.org/Talks/Tools/Slidy/
