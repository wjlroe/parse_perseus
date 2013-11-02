(ns parse_perseus.core
  (:use clojure.pprint
	[clojure.set :only [select]]))

;; Beta Code = bc = funny combinations of chars

(def iliad_first_line_bc "mh=nin a)/eide qea\\ *phlhi+a/dew *)axilh=os")
(def iliad_first_line_gk "μῆνιν ἄειδε θεὰ Πηληϊάδεω Ἀχιλῆος")
(def odyssey_first_line_bc "a)/ndra moi e)/nnepe, mou=sa, polu/tropon, o(\\s ma/la polla\\")
(def odyssey_first_line_gk "ἄνδρα μοι ἔννεπε, μοῦσα, πολύτροπον, ὃς μάλα πολλὰ")
(def odyssey_book18_l365_bc "e)/ris")
(def odyssey_book18_l365_gk "ἔρις")
(def ex-char "ἔ")

(defn int-to-hex [number]
  (Integer/toHexString (bit-and number 0xffff)))

(defn int-to-url-unicode [number]
  (format "%%u%04x" number))

(defn codepoints
  "Returns a sequence of integer Unicode code points in s.  Handles
  Unicode supplementary characters (above U+FFFF) correctly."
  [#^String s]
  (let [len (.length s)
        f (fn thisfn [#^String s i]
            (when (< i len)
              (let [c (.charAt s i)]
                (if (Character/isHighSurrogate c)
                  (cons (.codePointAt s i) (thisfn s (+ 2 i)))
                  (cons (int c) (thisfn s (inc i)))))))]
    (lazy-seq (f s 0))))

(defn unicode-points [text]
  (codepoints text))

(defn url-encoded-unicode [text]
  (apply str (map int-to-url-unicode (codepoints text))))

(defn bc-to-gk [bc-text]
  bc-text)
