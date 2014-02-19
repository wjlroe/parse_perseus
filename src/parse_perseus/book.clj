(ns parse_perseus.book
  (:use
    parse_perseus.betacode
    parse_perseus.utils
    clojure.xml
    [clojure.java.io :only [make-parents file copy delete-file]])
  (:require
    [clojure.data.xml :as xml]
    [hiccup.page :as page]
    [hiccup.core :as hiccup]
    [environ.core :as env]
    [clojure.stacktrace :as stack]
    [clojure.java.io :as io]
    [clojure.zip :as zip]
    [clojure.xml :as cxml]
    [parse_perseus.books :as books]
    [parse_perseus.epub :as epub])
  (:import
    [java.io File BufferedWriter FileWriter
     FileOutputStream FileNotFoundException]
    [java.util.zip ZipOutputStream ZipEntry]))

(def home (System/getProperty "user.home"))
(def covers-dir (or (some-> (env/env :perseus-covers-dir) file)
                    (file home "Dropbox/perseus")))
(def texts-dir (or (some-> (env/env :perseus-texts-dir) file)
                   (file home "Dropbox/perseus/texts")))
(def print-debug-messages (some-> (env/env :debug) (= "true")))
(def xml-options [:validating false
                  :namespace-aware false
                  :replacing-entity-references false
                  :supporting-external-entities false
                  :support-dtd false])

(defn startparse-sax-non-validating
  [s ch]
  (.. (doto (. javax.xml.parsers.SAXParserFactory (newInstance))
        (.setValidating false)
        (.setFeature "http://apache.org/xml/features/nonvalidating/load-dtd-grammar" false)
        (.setFeature "http://apache.org/xml/features/nonvalidating/load-external-dtd" false)
        (.setFeature "http://xml.org/sax/features/validation" false)
        (.setFeature "http://xml.org/sax/features/external-general-entities" false)
        (.setFeature "http://xml.org/sax/features/external-parameter-entities" false))
      (newSAXParser)
      (parse s ch)))

(defn ebook-dir
  [{:keys [identifier]}]
  (file "/tmp/perseus-books" identifier))

(defn ebook-filename
  [{:keys [identifier]}]
  (file "/tmp/perseus-books" (str identifier ".epub")))

(defn book-xml
  [{:keys [book-xml]}]
  (file texts-dir book-xml))

(defn cover-image-file
  [{:keys [cover-image]}]
  (file covers-dir cover-image))

(defn cover-image?
  [book]
  (.exists (cover-image-file book)))

(defn debug
  [& stuffs]
  (when print-debug-messages
    (apply println stuffs)))

(defstruct chapter
  :playorder
  :elem-id
  :filename)

(def ncx-doctype
  "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\" \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">\n")

(defn write-file
  [to-file data]
  (make-parents to-file)
  (with-open [wtr (BufferedWriter. (FileWriter. to-file))]
    (.write wtr data)))

(defn content-file
  [book-node]
  (for [x (:content book-node)
        :when (= :l (:tag x))
        :let [content (:content x)]]
    (if (= :milestone (:tag (first content)))
      (str "</p>\n<p>" (parse-bc (second content)))
      (parse-bc (first content)))))

(defn parse-book-xml
  [{:keys [book-xml] :as book}]
  (assoc book
         :chapters
         (for [book (xml-seq (cxml/parse book-xml startparse-sax-non-validating))
               :when (= :div1 (:tag book))]
           {:content (for [line (:content book)
                           :when (= :l (:tag line))]
                       (if (= :milestone (:tag (first line)))
                         (parse-bc (second line))
                         (parse-bc (first line))))})))

(defn chapter-files
  [{:keys [chapters] :as book}]
  (assoc book
         :chapter-files
         (map-indexed (fn [index chapter]
                        (let [chapter-name (str "chapter" (inc index))]
                          (assoc chapter
                                 :id chapter-name
                                 :filename (str chapter-name ".html"))))
                      chapters)))

(defn book-content
  [book lines chapter]
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

(defn chapter-generating-element
  [element]
  (or
    ; a div1 with type="Book"
    (and (= :div1 (:tag element)) (= "Book" (:type (:attrs element))))
    ; a text element with an n attribute
    (and (= :text (:tag element)) (contains? (:attrs element) :n))))

(defn bc-content-from-file
  [book]
  (assoc book
         :chapter-files
         (for [node (xml-seq (parse (book-xml book)))
               :when (chapter-generating-element node)
               :let [playorder (:n (:attrs node))
                     elem-id (str "book-" playorder)
                     chapter (struct-map chapter
                                         :playorder playorder
                                         :elem-id elem-id
                                         :filename (str elem-id ".xhtml"))]]
           (do
             (write-file (file (ebook-dir book)
                               (str "OPS/" (:filename chapter)))
                         (book-content book (content-file node) chapter))
             chapter))))

(defn book-opf
  [book]
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
        [:dc:identifier {:id (:identifier book) :opf:scheme "URL"}
         (:ident-url book)]
        [:dc:creator {:opf:file-as (:author book) :opf:role "aut"}
         (:author book)]
        (when (cover-image? book)
          [:meta {:name "cover" :content "cover-image"}])]
       [:manifest
        (for [chapter (:chapter-files book)]
          [:item {:id (:elem-id chapter)
                  :href (:filename chapter)
                  :media-type "application/xhtml+xml"}])
        [:item {:id "stylesheet"
                :href "style.css"
                :media-type "text/css"}]
        [:item {:id "ncx"
                :href "book.ncx"
                :media-type "application/x-dtbncx+xml"}]
        [:item {:id "cover"
                :href "cover.html"
                :media-type "application/xhtml+xml"}]
        [:item {:id "toc"
                :href "toc.html"
                :media-type "application/xhtml+xml"}]
        (when (cover-image? book)
          [:item {:id "cover-image"
                  :href "cover.jpg"
                  :media-type "image/jpeg"}])]
       [:spine {:toc "ncx"}
        [:itemref {:idref "cover" :linear "no"}]
        [:itemref {:idref "toc" :linear "no"}]
        (for [chapter (:chapter-files book)]
          [:itemref {:idref (:elem-id chapter)}])]
       [:guide
        [:reference {:href "cover.html" :type "cover" :title "Cover"}]
        [:reference {:href "toc.html" :type "toc" :title "Table of Contents"}]
        (let [chapter (first (:chapter-files book))]
          [:reference {:href (:filename chapter)
                       :type "text"
                       :title "Text"}])]])))

(defn cover-page
  [book]
  (page/xhtml
    {:doctype "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n"}
    [:head
     [:title "Cover"]
     [:style {:type "text/css"} "img { max-width: 100%; height: 100% }"]]
    [:body
     (when (cover-image? book)
       [:div {:id "cover-image"}
        [:img {:src "cover.jpg" :alt "Cover image"}]])]))

(defn table-of-contents
  [book]
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
          [:a {:href (:filename chapter)}
           (str "Book " (:playorder chapter))]])]]]))

(defn book-ncx
  [book]
  (hiccup/html
    {:mode :xml}
    (page/xml-declaration "UTF-8")
    ncx-doctype
    [:ncx {:version "2005-1"
           :xml:lang "en"
           :xmlns "http://www.daisy.org/z3986/2005/ncx/"}
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
        [:navPoint {:class "chapter"
                    :id (:elem-id chapter)
                    :playOrder (:playorder chapter)}
         [:navLabel [:text (str "Book " (:playorder chapter))]]
         [:content {:src (:filename chapter)}]])]]))

(defn container
  [book]
  (xml/emit-str
    (xml/sexp-as-element
      [:container {:version "1.0"
                   :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
       [:rootfiles
        [:rootfile {:full-path "OPS/book.opf"
                    :media-type "application/oebps-package+xml"}]]])))

(defn mimetype
  [book]
  "application/epub+zip
  ")

(defn stylesheet
  [book]
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

(defn write-book-opf
  [to-file book]
  (write-file to-file (book-opf book)))

(defn write-book-cover-page
  [to-file book]
  (write-file to-file (cover-page book)))

(defn copy-cover-image
  [book]
  (copy
    (file (file covers-dir (:cover-image book)))
    (file (ebook-dir book) "OPS/cover.jpg")))

(defn create-epub
  [book]
  (let [files (concat (keys epub-files)
                      (map #(str "OPS/" (:filename %))
                           (:chapter-files book)))
        files (if (cover-image? book)
                (concat files ["OPS/cover.jpg"])
                files)]
    (with-open [out (-> (ebook-filename book)
                        (FileOutputStream.)
                        (ZipOutputStream.))]
      (doseq [thisfile files
              :let [filename (file (ebook-dir book) thisfile)
                    entry (ZipEntry. thisfile)]]
        (when (= "metadata" thisfile)
          (.setMethod entry ZipEntry/STORED))
        (debug "About to put a file into zip :" (.getPath filename))
        (.putNextEntry out (ZipEntry. thisfile))
        (debug "Have put file: " (.getPath filename))
        (copy (file filename) out)
        (debug "EBook file generated: "
               (.getPath (ebook-filename book)))))))

(defn write-all-files
  [book]
  (doseq [thefilename (keys epub-files)
          :let [contents-fun (epub-files thefilename)]]
    (debug "About to write-file: " thefilename)
    (write-file (file (ebook-dir book) thefilename)
                (contents-fun book))))

(defn generate-book
  [book]
  (try
    (delete-file-recursively (ebook-dir book) true)
    (let [new-book (bc-content-from-file book)]
      (write-all-files new-book)
      (when (cover-image? book)
        (copy-cover-image new-book))
      (create-epub new-book))
    (catch FileNotFoundException e
      (stack/print-stack-trace e)
      (println "File not found: " (.getMessage e)))
    (finally
      (println "Done."))))

(defn tap-value
  [x msg]
  (println msg x)
  x)

(defn generate-book1
  [book]
  (-> book
      parse-book-xml
      (tap-value "after parse-book-xml:")
      chapter-files
      (tap-value "after chapter-files:")
      epub/write-epub))

;; TODO: Command line arguments - title, xml source, cover image
(defn -main
  [& args]
  (let [book-to-generate (some-> (first args) keyword)
        book (or (get books/books book-to-generate) :all)]
    (if (= book :all)
      (doseq [[name book] books/books]
        (generate-book book))
      (generate-book1 book))))
