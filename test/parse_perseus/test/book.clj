(ns parse_perseus.test.book
  (:use [parse_perseus.book] :reload)
  (:use expectations
        parse_perseus.test.test_helper)
  (:require [clojure.java.io :as io]))

(expect map? (parse-book-xml (book)))

;; Extract Books:
;; {:chapters [{:content []}]}
(expect :chapters (in (keys (parse-book-xml (book)))))

;; chapters contain content
(expect :content (in (keys (first (:chapters (parse-book-xml (book)))))))

;; chapters transformated to chapter-files
(expect :chapter-files (in (keys (chapter-files (parse-book-xml (book))))))

;; chapter-files contain ids and filenames
(given [key result] (expect key (in (keys (first (:chapter-files (chapter-files result))))))
       :id (parse-book-xml (book))
       :filename (parse-book-xml (book)))
