(ns parse_perseus.test.book
  (:use [parse_perseus.book] :reload)
  (:use clojure.test)
  (:use
   [clojure.contrib.io :only [file-str]]))

(defn fixture-contents
  [filename]
  (slurp (file-str (format "resources/test/%s" filename))))

(testing "book-xml -> structure of lines"
  (deftest book-xml-to-simple-structure
    (is (= (book-to-struct (fixture-contents "hom.od_gk.1line.xml"))
           [{:book 1
             :lines []}])))

  (deftest book-xml-to-null-structure
    (is (= (book-to-struct (fixture-contents "hom.od_gk.nothing.xml"))
           []))))
