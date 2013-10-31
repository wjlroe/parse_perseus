(ns parse_perseus.book
  (:use parse_perseus.betacode
        clojure.xml
        clojure.pprint
        [clojure.java.io :only [make-parents file copy delete-file]])
  (:require [clojure.data.xml :as xml]
            [hiccup.page :as page]
            [hiccup.core :as hiccup])
  (:import [java.io File BufferedWriter FileReader
            FileWriter FileOutputStream FileNotFoundException]
           [java.util.zip ZipOutputStream ZipEntry]))

(defstruct book
  :title
  :identifier
  :ident-url
  :author
  :cover-image
  :epub-filename
  :epub-dir
  :book-xml
  :chapter-files)

(defstruct chapter
  :playorder
  :elem-id
  :filename)

(def ncx-doctype
  "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n")

(defn write-file [to-file data]
  (do
    (make-parents to-file)
    (with-open [wtr (BufferedWriter. (FileWriter. to-file))]
      (.write wtr data))))

(defn content-file [book-node]
  (for [x (:content book-node)
        :when (= :l (:tag x))]
    (let [content (:content x)]
      (if (= :milestone (:tag (first content)))
        (cons [:raw! "</p>\n<p>"] (parse-bc (second content)))
        (parse-bc (first content))))))

(defn book-content [book lines chapter]
  (page/xhtml
    {:doctype "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" :lang "grc"}
    [:head
     [:meta {:http-equiv "Content-Type"
             :content "application/xhtml+xml; charset=utf-8"}]
     [:title (:title book)]
     [:link {:rel "stylesheet"
             :href "style.css"
             :type "text/css"}]]
    [:body
     [:h1 (str "Book " (:playorder chapter))]
     [:p
      (for [line lines] (cons line (cons [:br] "\n")))]]))

(defn bc-content-from-file [book]
  (let [files
        (for [node (xml-seq (parse (file (:book-xml book))))
              :when (and (= :div1 (:tag node)) (= "Book" (:type (:attrs node))))]
          (let [playorder (:n (:attrs node))
                elem-id (str "book-" playorder)
                chapter (struct-map chapter
                                    :playorder playorder
                                    :elem-id elem-id
                                    :filename (str elem-id ".xhtml"))]
            (do
              (write-file (str (book :epub-dir) "/OPS/" (:filename chapter))
                          (book-content book (content-file node) chapter))
              chapter)))]
    (assoc book :chapter-files files)))

(defn book-opf [book]
  (xml/emit-str
    (xml/sexp-as-element
      [:package {:version "2.0"
                 :xmlns "http://www.idpf.org/2007/opf"
                 :unique-identifier (:identifier book)}
       [:metadata {:xmlns:dc "http://purl.org/dc/elements/1.1/"
                   :xmlns:dcterms "http://purl.org/dc/terms/"
                   :xmlns:opf "http://www.idpf.org/2007/opf"
                   :xmlns:xsi "http://www.w3.org/2001/XMLSchema-instance"}
        [:dc:title (:title book)]
        [:dc:language {:xsi:type "dcterms:RFC3066"} "en-us"]
        [:dc:identifier {:id (:identifier book) :opf:scheme "URL"} (:ident-url book)]
        [:dc:creator {:opf:file-as (:author book) :opf:role "aut"} (:author book)]
        [:meta {:name "cover" :content "cover-image"}]]
       [:manifest
        (for [chapter (:chapter-files book)]
          [:item {:id (:elem-id chapter) :href (:filename chapter) :media-type "application/xhtml+xml"}])
        [:item {:id "stylesheet" :href "style.css" :media-type "text/css"}]
        [:item {:id "ncx" :href "book.ncx" :media-type "application/x-dtbncx+xml"}]
        [:item {:id "cover" :href "cover.html" :media-type "application/xhtml+xml"}]
        [:item {:id "toc" :href "toc.html" :media-type "application/xhtml+xml"}]
        [:item {:id "cover-image" :href "cover.jpg" :media-type "image/jpeg"}]]
       [:spine {:toc "ncx"}
        [:itemref {:idref "cover" :linear "no"}]
        [:itemref {:idref "toc" :linear "no"}]
        (for [chapter (:chapter-files book)]
          [:itemref {:idref (:elem-id chapter)}])]
       [:guide
        [:reference {:href "cover.html" :type "cover" :title "Cover"}]
        [:reference {:href "toc.html" :type "toc" :title "Table of Contents"}]
        (let [chapter (first (:chapter-files book))]
          [:reference {:href (:filename chapter) :type "text" :title "Text"}])]])))

(defn cover-page [book]
  (page/xhtml
    {:doctype "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"}
    [:head
     [:title "Cover"]
     [:style {:type "text/css"} "img { max-width: 100%; height: 100% }"]]
    [:body
     [:div {:id "cover-image"}
      [:img {:src "cover.jpg" :alt "Cover image"}]]]))

(defn table-of-contents [book]
  (page/xhtml
    {:doctype "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"}
    [:head
     [:title "Table of Contents"]
     [:style {:type "text/css"} "img { max-width: 100%; height: 100% }"]]
    [:body
     [:div {:id "contents"}
      [:h2 "Contents"]
      [:ul
       (for [chapter (:chapter-files book)]
         [:li
          [:a {:href (:filename chapter)} (str "Book " (:playorder chapter))]])]]]))

(defn book-ncx [book]
  (hiccup/html
    {:mode :xml}
    (page/xml-declaration "UTF-8")
    ncx-doctype
    [:ncx {:version "2005-1" :xml:lang "en" :xmlns "http://www.daisy.org/z3986/2005/ncx/"}
     [:head
      [:meta {:name "dtb:uid" :content (:ident-url book)}]
      [:meta {:name "dtb:depth" :content "1"}]
      [:meta {:name "dtb:totalPageCount" :content "0"}]
      [:meta {:name "dtb:maxPageNumber" :content "0"}]]
     [:docTitle
      [:text (:title book)]]
     [:navMap
      [:navPoint {:id "toc" :playOrder "0"}
       [:navLabel [:text "Table of Contents"]]
       [:content {:src "toc.html"}]]
      (for [chapter (:chapter-files book)]
        [:navPoint {:class "chapter" :id (:elem-id chapter) :playOrder (:playorder chapter)}
         [:navLabel [:text (str "Book " (:playorder chapter))]]
         [:content {:src (:filename chapter)}]])]]))

(defn container [book]
  (xml/emit-str
    (xml/sexp-as-element
      [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
       [:rootfiles
        [:rootfile {:full-path "OPS/book.opf" :media-type "application/oebps-package+xml"}]]])))

(defn mimetype [book]
  "application/epub+zip
  ")
(defn stylesheet [book]
  "p
  {
  text-indent:            0em;
  margin-top:             0.5em;
  margin-bottom:          0.5em;
  }")

(def epub-files
  {"mimetype" mimetype
   "OPS/book.opf" book-opf
   "OPS/book.ncx" book-ncx
   "OPS/style.css" stylesheet
   "OPS/cover.html" cover-page
   "OPS/toc.html" table-of-contents
   "META-INF/container.xml" container})

(defn write-book-opf [to-file book]
  (write-file to-file (book-opf book)))

(defn write-book-cover-page [to-file book]
  (write-file to-file (cover-page book)))

(defn copy-cover-image [book]
  (copy
    (file (:cover-image book))
    (file (str (:epub-dir book) "/OPS/cover.jpg"))))

(defn create-epub [book]
  (with-open [out (-> (file (:epub-filename book))
                      (FileOutputStream.)
                      (ZipOutputStream.))]
    (dorun
      (for [thisfile (concat (keys epub-files) ["OPS/cover.jpg"] (map #(str "OPS/" (:filename %)) (:chapter-files book)))
            :let [filename (str (:epub-dir book) "/" thisfile)]]
        (let [entry (ZipEntry. thisfile)]
          (do
            (if (= "metadata" thisfile)
              (.setMethod entry ZipEntry/STORED))
            (println "About to put a file into zip :" filename)
            (.putNextEntry out (ZipEntry. thisfile))
            (println "Have put file: " filename)
            (copy (file filename) out)))))))

(defn write-all-files [book]
  (doall
    (for [thefilename (keys epub-files)
          :let [contents-fun (epub-files thefilename)]]
      (do
        (println "About to write-file: " thefilename)
        (write-file (str (book :epub-dir) "/" thefilename) (contents-fun book))))))

(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its contents.
  Raise an exception if any deletion fails unless silently is true."
  [f & [silently]]
  (let [f (file f)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child silently)))
    (delete-file f silently)))

;; TODO: Command line arguments - title, xml source, cover image
(defn -main []
  (try
    (let [home (System/getProperty "user.home")
          book (struct-map book
                           :title "Ὀδύσσεια"
                           :identifier "odyssey_gk"
                           :ident-url "http://en.wikipedia.org/wiki/The_Odyssey"
                           :author "Homer"
                           :cover-image (str home "/Dropbox/perseus/odyssey.jpg")
                           :book-xml (str home "/Dropbox/perseus/texts/Classics/Homer/opensource/hom.od_gk.xml")
                           :epub-dir "/tmp/epub-book"
                           :epub-filename "/tmp/book.epub")]
      (do
        (delete-file-recursively (:epub-dir book) true)
        (let [new-book (bc-content-from-file book)]
          (do
            (pprint new-book)
            (write-all-files new-book)
            (copy-cover-image new-book)
            (create-epub new-book)))))
    (catch FileNotFoundException e
      (do
        (println "File not found: " (.getMessage e))))
    (finally
      (println "Done."))))
