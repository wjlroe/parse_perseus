(ns parse_perseus.book
  (:use parse_perseus.betacode
	clojure.xml
	clojure.pprint
	[clojure.contrib.duck-streams :only [write-lines]]
	clojure.contrib.prxml)
  (:import [java.io File BufferedWriter FileWriter]))

(defstruct book
  :title
  :identifier
  :ident-url
  :author
  :cover-image)

(defn bc-content-from-file [filename]
  (for [x (xml-seq (parse (File. filename)))
	:when (= :l (:tag x))]
    (let [content (:content x)]
      (first (if (= :milestone (:tag (first content)))
	       (:content (first content))
	       (:content x))))))

(defn bc-file-to-gk [filename]
  (map parse-bc (remove nil? (bc-content-from-file filename))))

(defn book-content [filename book]
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
		     :href "css/main.css"
		     :type "text/css"}]]
	    [:body
	     (for [line (bc-file-to-gk filename)] (cons line [:br]))]])))

(defn book-opf [title identifier author cover-image ident-url]
  (with-out-str
    (prxml [:decl! {:version "1.0"}]
	   [:package {:version "2.0"
		      :xmlns "http://www.idpf.org/2007/opf"
		      :unique-identifier (:identifier book)}
	    [:metadata {:xmlns:dc="http://purl.org/dc/elements/1.1/"
			:xmlns:dcterms="http://purl.org/dc/terms/"
			:xmlns:opf="http://www.idpf.org/2007/opf"
			:xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"}
	     [:dc:title (:title book)]
	     [:dc:language {:xsi:type "dcterms:RFC3066"} "en-us"]
	     [:dc:identifier {:id (:identifier book) :opf:scheme "URL"} (:ident-url book)]
	     [:dc:creator {:opf:file-as "Homer" :opf:role "aut"} (:author book)]
	     [:meta {:name "cover" :content "cover-image"}]]
	    [:manifest
	     [:item {:id "book-content"
		     :href "book-content.xhtml"
		     :media-type "application/xhtml+xml"}]
	     [:item {:id "stylesheet" :href "style.css" :media-type "text/css"}]
	     [:item {:id "ncx" :href "book.ncx" :media-type "application/x-dtbncx+xml"}]
	     [:item {:id "cover" :href "cover.html" :media-type "application/xhtml+xml"}]
	     [:item {:id "cover-image" :href "odyssey.jpg" :media-type "image/jpeg"}]]
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
	      [:img {:src (:cover-image book) :alt "Cover image"}]]]])))

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

(defn container []
  (with-out-str
    (prxml [:decl! {:version "1.0" :encoding "UTF-8"}]
	   [:container {:version "1.0" :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
	    [:rootfiles
	     [:rootfile {:full-path "OPS/book.opf" :media-type "application/oebps-package+xml"}]]])))

(def mimetype "application/epub+zip")
(def stylesheet "")

(defn write-file [to-file data]
  (with-open [wtr (BufferedWriter. (FileWriter. to-file))]
    (.write wtr data)))

(defn write-book-content [from-file to-file book]
  (write-file to-file (book-content from-file book)))

(defn write-book-opf [to-file book]
  (write-file to-file (book-opf book)))

(defn write-book-cover-page [to-file book]
  (write-file to-file (cover-page book)))

;; TODO: Command line arguments - title, xml source, cover image
;; TODO: How to build the ZIP file (with mimetype file first)
(defn -main []
  )

