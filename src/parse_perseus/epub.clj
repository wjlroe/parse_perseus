(ns parse_perseus.epub
  (:use
    [clojure.java.io :only [file make-parents copy]])
  (:require
    [clojure.string :as string]
    [selmer.filters :as filters]
    [selmer.parser :as selmer])
  (:import
    [java.io FileOutputStream]
    [java.util.zip ZipOutputStream ZipEntry]))

(defn default-fields
  [book]
  (assoc book :files []))

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
  (println-str "application/epub+zip"))

(def epub-files
  {"mimetype" mimetype
   "OPS/book.opf" ops-book-opf
   "OPS/book.ncx" ops-book-ncx
   "OPS/style.css" ops-style
   "OPS/cover.html" ops-cover
   "OPS/toc.html" ops-toc
   "META-INF/container.xml" meta-container})

(defn create-epub-files
  [{:keys [ebook-location files] :as book}]
  (doseq [[filename content-fun] epub-files
          :let [filename (str (file ebook-location filename))]]
    (make-parents filename)
    (spit filename (content-fun book)))
  (assoc book :files (into files (keys epub-files))))

(defn create-chapter-files
  [{:keys [ebook-location chapter-files] :as book}]
  (doseq [{:keys [filename] :as chapter} chapter-files
          :let [filename (str (file ebook-location "OPS" filename))
                book' (assoc book :chapter chapter)]]
    (make-parents filename)
    (spit filename (ops-chapter book')))
  book)

(defn cover-image
  [{:keys [ebook-location cover-image covers-dir] :as book}]
  (when cover-image
    (copy (str (file covers-dir cover-image)) (file ebook-location "OPS" cover-image)))
  book)

(defn ebook-location
  [{:keys [ebooks-location identifier] :as book}]
  {:pre [(string? ebooks-location)
         (string? identifier)]}
  (assoc book
         :ebook-location (file ebooks-location identifier)
         :ebook-filename (file ebooks-location (str identifier ".epub"))))

(defn zip-entry
  [filename]
  (ZipEntry. filename))

(defn add-file-to-zip
  [output ebook-file input-file]
  (let [entry (zip-entry ebook-file)]
    (when (.exists input-file)
      (when (= "mimetype" ebook-file)
        (.setMethod entry ZipEntry/STORED))
      (.putNextEntry output entry)
      (copy input-file output))))

(defn create-epub
  [{:keys [files ebook-filename ebook-location]}]
  (with-open [out (-> (FileOutputStream. ebook-filename)
                      (ZipOutputStream.))]
    (doseq [ebook-file files
            :let [input-file (file ebook-location ebook-file)]]
      (add-file-to-zip out ebook-file input-file))))

(defn write-epub
  [book]
  (-> book
      default-fields
      ebook-location
      cover-image
      create-epub-files
      create-chapter-files
      create-epub))
