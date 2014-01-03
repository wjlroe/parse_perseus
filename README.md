# parse_perseus

[![Build Status](https://secure.travis-ci.org/wjlroe/parse_perseus.png)](http://travis-ci.org/wjlroe/parse_perseus)

Perseus is a digital library of Ancient Greek and Roman texts. This project is for converting the XML files, freely available from their website, into Unicode ebooks for e-readers such as the Amazon Kindle.

See [Parsing and ebook making](http://wjlroe.github.com/2011/04/25/parsing-and-ebook-making.html) which is a blog entry I wrote about this project.

## Usage

The paths to the Perseus XML files is hardcoded in [parse_perseus.book](https://github.com/wjlroe/parse_perseus/blob/master/src/parse_perseus/book.clj).

- To run the parser and produce an epub book, run: `lein run` which will run the default command in project.clj
- To run the tests, run the following: `lein tests`

## Code organisation

The parsing of the Perseus XML files and generation of the ePub files are
processed in stages:

1. XML -> book structure: This stage lifts the content from the XML files into
   an internal representation of a book complete with chapters/books and lines
2. book (betacode) -> book (unicode): This stage parses Greek betacode and
   translates it to unicode Greek. On other languages, it does nothing.
3. book -> ePub: This stage generates a `.epub` file at the end containing the
   structure from the original XML files, parsed and in unicode.

## License

<a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/">Creative Commons Attribution-ShareAlike 3.0 Unported License</a>.
