(ns parse_perseus.test.test_helper
  (:require
    [clojure.string :as string])
  (:import
    java.io.File
    java.util.regex.Matcher))

(defn book
  ([] (book {}))
  ([overrides]
   ;; fixture filename location
   (merge
     {:book-xml "simple.xml"
      :title "Odyssey"
      :identifier "the_odyssey"
      :ebooks-location "/tmp/perseus-tests"
      :covers-dir "/tmp/perseus-test-covers"
      :author "Homer"}
     overrides)))

(defn re-qr
  [replacement]
  (Matcher/quoteReplacement replacement))

(defn pathname
  [name]
  (if (= "\\" File/separator)
    (string/replace name #"/" (re-qr File/separator))
    name))

(defn regexpath
  [name]
  (re-pattern (re-qr (pathname name))))
