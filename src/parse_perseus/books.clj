(ns parse_perseus.books)

(def home (System/getProperty "user.home"))

(def books
  {:the_odyssey
   {:title "Ὀδύσσεια"
    :identifier "odyssey_gk"
    :ident-url "http://en.wikipedia.org/wiki/The_Odyssey"
    :author "Homer"
    :cover-image "odyssey.jpg"
    :book-xml "Classics/Homer/opensource/hom.od_gk.xml"}
   :xenophon_minor
   {:title "Minor Works"
    :identifier "opuscula_gk"
    :ident-url "http://www.perseus.tufts.edu/hopper/search?doc=Perseus%3atext%3a1999.01.0209"
    :author "Xenophon"
    :cover-image "minor_works.jpg"
    :book-xml "Classics/Xenophon/opensource/xen.opuscula_gk.xml"}})
