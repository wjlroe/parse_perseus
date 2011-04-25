(ns parse_perseus.book
  (:use parse_perseus.betacode
	clojure.xml
	clojure.pprint
	[clojure.java.io :only [make-parents file]]
	[clojure.contrib.duck-streams :only [write-lines copy reader writer]]
	[clojure.contrib.io :only [delete-file-recursively]]
	clojure.contrib.prxml)
  (:import [java.io File BufferedWriter FileReader FileWriter FileOutputStream FileNotFoundException]
	   [java.util.zip ZipOutputStream ZipEntry]))

(defstruct book
  :title
  :identifier
  :ident-url
  :author
  :cover-image
  :epub-filename
  :epub-dir
  :book-xml)

(defn bc-content-from-file [filename]
  (for [x (xml-seq (parse (file filename)))
	:when (= :l (:tag x))]
    (let [content (:content x)]
      (if (= :milestone (:tag (first content)))
	(cons [:raw! "</p>\n<p>"] (parse-bc (second content)))
	(parse-bc (first content))))))

;; (defn bc-file-to-gk [filename]
;;   (map parse-bc (remove nil? (bc-content-from-file filename))))

(defn book-content [book]
  (with-out-str
    (prxml [:decl! {:version "1.0"}]
	   [:doctype! "html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\""]
	   [:html {:xmlns "http://www.w3.org/1999/xhtml"
		   :xml:lang "grc"}
	    [:head
	     [:meta {:http-equiv "Content-Type"
		     :content "application/xhtml+xml; charset=utf-8"}]
	     [:title (:title book)]
	     [:link {:rel "stylesheet"
		     :href "style.css"
		     :type "text/css"}]]
	    [:body
	     [:p
	      (for [line (bc-content-from-file (:book-xml book))] (cons line (cons [:br] "\n")))]]])))

(defn book-opf [book]
  (with-out-str
    (prxml [:decl! {:version "1.0"}]
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
	     [:item {:id "book-content"
		     :href "book-content.xhtml"
		     :media-type "application/xhtml+xml"}]
	     [:item {:id "stylesheet" :href "style.css" :media-type "text/css"}]
	     [:item {:id "ncx" :href "book.ncx" :media-type "application/x-dtbncx+xml"}]
	     [:item {:id "cover" :href "cover.html" :media-type "application/xhtml+xml"}]
	     [:item {:id "cover-image" :href "cover.jpg" :media-type "image/jpeg"}]]
	    [:spine {:toc "ncx"}
	     [:itemref {:idref "cover" :linear "no"}]
	     [:itemref {:idref "book-content"}]]
	    [:guide
	     [:reference {:href "cover.html" :type "cover" :title "Cover"}]]])))

(defn cover-page [book]
  (with-out-str
    (prxml [:doctype! "html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\""]
	   [:html {:xmlns "http://www.w3.org/1999/xhtml"}
	    [:head
	     [:title "Cover"]
	     [:style {:type "text/css"} "img { max-width: 100%; height: 100% }"]]
	    [:body
	     [:div {:id "cover-image"}
	      [:img {:src "cover.jpg" :alt "Cover image"}]]]])))

(defn book-ncx [book]
  (with-out-str
    (prxml [:decl! {:version "1.0" :encoding "UTF-8"}]
	   [:doctype! "ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\"
	    \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\""]
	   [:ncx {:version "2005-1" :xml:lang "en" :xmlns "http://www.daisy.org/z3986/2005/ncx/"}
	    [:head
	     [:meta {:name "dtb:uid" :content (:ident-url book)}]
	     [:meta {:name "dtb:depth" :content "1"}]
	     [:meta {:name "dtb:totalPageCount" :content "0"}]
	     [:meta {:name "dtb:maxPageNumber" :content "0"}]]
	    [:docTitle
	     [:text (:title book)]]
	    [:navMap
	     [:navPoint {:class "chapter" :id "book-content" :playOrder "1"}
	      [:navLabel [:text "Chapter 1"]]
	      [:content {:src "book-content.xhtml"}]]]])))

(defn container [book]
  (with-out-str
    (prxml [:decl! {:version "1.0" :encoding "UTF-8"}]
	   [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
	    [:rootfiles
	     [:rootfile {:full-path "OPS/book.opf" :media-type "application/oebps-package+xml"}]]])))

(defn mimetype [book]
  "application/epub+zip")
(defn stylesheet [book]
  "")

(def epub-files
     {"mimetype" mimetype
      "OPS/book.opf" book-opf
      "OPS/book-content.xhtml" book-content
      "OPS/book.ncx" book-ncx
      "OPS/style.css" stylesheet
      "OPS/cover.html" cover-page
      "META-INF/container.xml" container})

(defn write-file [to-file data]
  (do
    (make-parents to-file)
    (with-open [wtr (BufferedWriter. (FileWriter. to-file))]
      (.write wtr data))))

(defn write-book-content [from-file to-file book]
  (write-file to-file (book-content from-file book)))

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
      (for [thisfile (concat (keys epub-files) ["OPS/cover.jpg"])
            :let [filename (str (:epub-dir book) "/" thisfile)]]
        (do
          (println "About to put a file into zip :" filename)
          (.putNextEntry out (ZipEntry. thisfile))
          (println "Have put file: " filename)
          (copy (file filename) out))))))

(defn write-all-files [book]
  (doall
   (for [thefilename (keys epub-files)
	 :let [contents-fun (epub-files thefilename)]]
     (do
       (println "About to write-file: " thefilename)
       (write-file (str (book :epub-dir) "/" thefilename) (contents-fun book))))))

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
                           :book-xml (str home "/Desktop/texts/Classics/Homer/opensource/hom.od_gk.xml")
                           :epub-dir "/tmp/epub-book"
                           :epub-filename "/tmp/book.epub")]
      (do
        (delete-file-recursively (:epub-dir book) true)
        (write-all-files book)
        (copy-cover-image book)
        (create-epub book)))
    (catch FileNotFoundException e
      (do
        (println "File not found: " (.getMessage e))))
    (finally
      (println "Done."))))



