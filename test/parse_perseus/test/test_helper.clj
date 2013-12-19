(ns parse_perseus.test.test_helper)

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
