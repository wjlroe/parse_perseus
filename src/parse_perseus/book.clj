(ns parse_perseus.book
  (:use parse_perseus.betacode
	clojure.xml
	clojure.pprint
	[clojure.contrib.duck-streams :only [write-lines]])
  (:import [java.io File]))

(defn bc-content-from-file [filename]
  (for [x (xml-seq (parse (File. filename)))
	:when (= :l (:tag x))]
    (let [content (:content x)]
      (first (if (= :milestone (:tag (first content)))
	       (:content (first content))
	       (:content x))))))

(defn bc-file-to-gk [filename]
  (map parse-bc (remove nil? (bc-content-from-file filename))))

(defn -main []
  (let [lines (bc-file-to-gk "/Users/will/Desktop/texts/Classics/Homer/opensource/hom.od_gk.xml")]
    (write-lines "/tmp/odyssey.html" lines)))

