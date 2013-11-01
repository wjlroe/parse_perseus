(ns parse_perseus.books)

(def home (System/getProperty "user.home"))

(def books
  {:the_odyssey
   {:title "Ὀδύσσεια"
    :identifier "odyssey_gk"
    :ident-url "http://en.wikipedia.org/wiki/The_Odyssey"
    :author "Homer"
    :cover-image (str home "/Dropbox/perseus/odyssey.jpg")
    :book-xml (str home "/Dropbox/perseus/texts/Classics/Homer/opensource/hom.od_gk.xml")
    :epub-dir "/tmp/epub-book"
    :epub-filename "/tmp/book.epub"}})
