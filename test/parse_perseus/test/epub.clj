(ns parse_perseus.test.epub
  (:use [parse_perseus.epub] :reload)
  (:use expectations
        parse_perseus.test.test_helper))

;; author-file-as
(expect "Austen, Jane"
        (author-file-as "Jane Austen"))
(expect "Homer"
        (author-file-as "Homer"))

;; OPS/book.ncx
(expect #"Super awesome funtime"
        (ops-book-ncx (book {:title "Super awesome funtime"})))
(expect #"chapter1.html"
        (ops-book-ncx (book {:chapter-files [{:filename "chapter1.html"}]})))
(expect #"Chapter 1"
        (ops-book-ncx (book {:chapter-files [{:title "Chapter 1"}]})))

;; OPS/book.opf
(expect #"unique-identifier=\"my_book_id\""
        (ops-book-opf (book {:identifier "my_book_id"})))
(expect #"<dc:title>Super fun</dc:title>"
        (ops-book-opf (book {:title "Super fun"})))
(expect #"http://woohoo.com</dc:identifier>"
        (ops-book-opf (book {:ident-url "http://woohoo.com"})))
(expect #"dc:identifier id=\"my_book_id\" opf:scheme=\"URL\">"
        (ops-book-opf (book {:identifier "my_book_id"})))
(expect #"<dc:creator opf:file-as=\"Lovelace, Ada\" opf:role=\"aut\">Ada Lovelace</dc:creator>"
        (ops-book-opf (book {:author "Ada Lovelace"})))
(expect #"<meta name=\"cover\" content=\"cover-image\" />"
        (ops-book-opf (book {:cover-image "cover.jpg"})))
(expect nil?
        (re-find #"<meta name=\"cover\""
                 (ops-book-opf (dissoc (book {}) :cover-image))))
(expect #"item id=\"cover-image\" href=\"cover2.jpg\""
        (ops-book-opf (book {:cover-image "cover2.jpg"})))
(expect nil?
        (re-find #"<item id=\"cover-image\""
                 (ops-book-opf (dissoc (book {}) :cover-image))))
(expect #"<itemref idref=\"chapter2\" />"
        (ops-book-opf (book {:chapter-files [{:id "chapter1"} {:id "chapter2"}]})))
(expect #"<reference href=\"chapter1.html\" type=\"text\" title=\"Text\" />"
        (ops-book-opf (book {:chapter-files [{:filename "chapter1.html"}]})))

;; OPS/toc.html
(expect #"<a href=\"chapter1.html\">Chapter 1</a>"
        (ops-toc (book {:chapter-files [{:filename "chapter1.html"
                                         :title "Chapter 1"}]})))

;; OPS/cover.html
(expect #"<img src=\"the-cover.jpg\" alt=\"Cover image\" />"
        (ops-cover (book {:cover-image "the-cover.jpg"})))
(expect nil?
        (re-find #"<div id=\"cover-image\">"
                 (ops-cover (dissoc (book {}) :cover-image))))

;; OPS/chapter.html
(expect #"Super happy fun times"
        (ops-chapter (book {:chapter {:contents "Wow. Super happy fun times."}})))
(expect #"Chapter 1"
        (ops-chapter (book {:chapter {:title "Chapter 1"}})))
(expect #"Book Title"
        (ops-chapter (book {:title "Book Title"})))

;; write-epub
(expect (interaction (spit #"OPS/book.opf" anything&))
        (doall (write-epub (book))))
(expect (interaction (spit #"OPS/book.ncx" anything&))
        (doall (write-epub (book))))
(expect (interaction (spit #"OPS/toc.html" anything&))
        (doall (write-epub (book))))
(expect (interaction (spit #"OPS/style.css" anything&))
        (doall (write-epub (book))))
(expect (interaction (spit #"OPS/cover.html" anything&))
        (doall (write-epub (book))))
(expect (interaction (spit #"META-INF/container.xml" anything&))
        (doall (write-epub (book))))
(expect (interaction (spit #"mimetype" anything&))
        (doall (write-epub (book))))
(expect (interaction (spit #"OPS/chapter1.html" anything&))
        (doall (write-epub (book {:chapter-files [{:filename "chapter1.html"}]}))))
(expect (interaction (clojure.java.io/copy #"cover-image.jpg" anything&))
        (doall (write-epub (book {:cover-image "cover-image.jpg"}))))
