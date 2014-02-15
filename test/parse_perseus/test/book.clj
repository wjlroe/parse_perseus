(ns parse_perseus.test.book
  (:use [parse_perseus.book] :reload)
  (:use expectations
        parse_perseus.test.test_helper)
  (:require [clojure.java.io :as io]))

(expect map? (parse-book-xml (book)))

;; Extract Books:
;; {:chapters [{:content []}]}
(expect :chapters (in (-> (book)
                          parse-book-xml
                          keys)))

;; chapters contain content
(expect :content (in (-> (book)
                         parse-book-xml
                         :chapters
                         first
                         keys)))

;; chapters transformated to chapter-files
(expect :chapter-files (in (-> (book)
                               parse-book-xml
                               chapter-files
                               keys)))

;; chapter-files contain ids and filenames
(given [key result] (expect key (in (-> result
                                        :chapter-files
                                        first
                                        keys)))
       :id (chapter-files (parse-book-xml (book)))
       :filename (chapter-files (parse-book-xml (book))))
