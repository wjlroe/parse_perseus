# parse_perseus

Perseus is a digital library of Ancient Greek and Roman texts. This project is for converting the XML files, freely available from their website, into Unicode ebooks for e-readers such as the Amazon Kindle.

## Usage

The paths to the Perseus XML files is hardcoded in [https://github.com/wjlroe/parse_perseus/blob/master/src/parse_perseus/book.clj](src/parse_perseus/book.clj).

- To run the parser and produce an epub book, run: `lein run` which will run the default command in project.clj
- To run the tests, run the following: `lein tests`

## License

<a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-sa/3.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/">Creative Commons Attribution-ShareAlike 3.0 Unported License</a>.
