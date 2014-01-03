(ns parse_perseus.epub
  (:use
    [clojure.java.io :only [file make-parents copy]])
  (:require
    [clojure.string :as string]
    [selmer.filters :as filters]
    [selmer.parser :as selmer]))

(defn author-file-as
  [author-name]
  (let [name-split (.split author-name " ")
        last-name (last name-split)
        everything-else (string/join " " (butlast name-split))]
    (if (> (count name-split) 1)
      (format "%s, %s" last-name everything-else)
      last-name)))

(filters/add-filter! :author-file-as author-file-as)

(def meta-container (partial selmer/render-file "ebook/META-INF/container.xml"))
(def ops-book-opf   (partial selmer/render-file "ebook/OPS/book.opf"))
(def ops-toc        (partial selmer/render-file "ebook/OPS/toc.html"))
(def ops-cover      (partial selmer/render-file "ebook/OPS/cover.html"))
(def ops-style      (partial selmer/render-file "ebook/OPS/style.css"))
(def ops-book-ncx   (partial selmer/render-file "ebook/OPS/book.ncx"))
(def ops-chapter    (partial selmer/render-file "ebook/OPS/chapter.html"))

(defn mimetype [&args]
  "application/epub+zip
  ")

(def epub-files
  {"mimetype" mimetype
   "OPS/book.opf" ops-book-opf
   "OPS/book.ncx" ops-book-ncx
   "OPS/style.css" ops-style
   "OPS/cover.html" ops-cover
   "OPS/toc.html" ops-toc
   "META-INF/container.xml" meta-container})

(defn create-epub-files
  [{:keys [ebook-location] :as book}]
  (for [[filename content-fun] epub-files]
    (let [filename (str (file ebook-location filename))]
      (make-parents filename)
      (spit filename (content-fun book)))))

(defn create-chapter-files
  [{:keys [ebook-location chapter-files] :as book}]
  (for [{:keys [filename] :as chapter} chapter-files]
    (let [filename (str (file ebook-location "OPS" filename))
          book (assoc book :chapter chapter)]
      (make-parents filename)
      (spit filename (ops-chapter book)))))

(defn write-epub
  [{:keys [ebooks-location identifier covers-dir cover-image] :as book}]
  (let [ebook-location (file ebooks-location identifier)
        book (assoc book :ebook-location ebook-location)]
    (concat
      (when cover-image
        (copy (str (file covers-dir cover-image)) (file ebook-location "OPS" cover-image)))
      (create-epub-files book)
      (create-chapter-files book))))