(ns parse_perseus.core
  (:use clojure.pprint
	[clojure.contrib.string :only [codepoints]]
	[clojure.set :only [select]]))

;; Beta Code = bc = funny combinations of chars

(def iliad_first_line_bc "mh=nin a)/eide qea\\ *phlhi+a/dew *)axilh=os")
(def iliad_first_line_gk "μῆνιν ἄειδε θεὰ Πηληϊάδεω Ἀχιλῆος")
(def odyssey_first_line_bc "a)/ndra moi e)/nnepe, mou=sa, polu/tropon, o(\\s ma/la polla\\")
(def odyssey_first_line_gk "ἄνδρα μοι ἔννεπε, μοῦσα, πολύτροπον, ὃς μάλα πολλὰ")
(def odyssey_book18_l365_bc "e)/ris")
(def odyssey_book18_l365_gk "ἔρις")
(def ex-char "ἔ")

(def beta-map
     #{{:beta \a :greek 0x03b1}
       {:beta \b :greek 0x03b2}
       {:beta \g :greek 0x03b3}
       {:beta \d :greek 0x03b4}
       {:beta \e :greek 0x03b5}
       {:beta \z :greek 0x03b6}
       {:beta \h :greek 0x03b7}
       {:beta \q :greek 0x03b8}
       {:beta \i :greek 0x03b9}
       {:beta \k :greek 0x03ba}
       {:beta \l :greek 0x03bb}
       {:beta \m :greek 0x03bc}
       {:beta \n :greek 0x03bd}
       {:beta \c :greek 0x03be}
       {:beta \o :greek 0x03bf}
       {:beta \p :greek 0x03c0}
       {:beta \r :greek 0x03c1}
       {:beta \s :greek 0x03c3}
       {:beta \t :greek 0x03c4}
       {:beta \u :greek 0x03c5}
       {:beta \f :greek 0x03c6}
       {:beta \x :greek 0x03c7}
       {:beta \y :greek 0x03c8}
       {:beta \w :greek 0x03c9}
       {:beta \' :greek 0x1fbd}})
;       {:beta \' :greek 0x0313}})

(defn int-to-hex [number]
  (Integer/toHexString (bit-and number 0xffff)))

(defn int-to-url-unicode [number]
  (format "%%u%04x" number))

(defn beta-char-to-greek-char [char]
  (-> (select #(= (:beta %) char) beta-map)
      first
      :greek))

(defn bc-string-to-gk [code]
  (apply str (map #(char (beta-char-to-greek-char %)) code)))

(defn unicode-points [text]
  (codepoints text))

(defn url-encoded-unicode [text]
  (apply str (map int-to-url-unicode (codepoints text))))

(defn bc-to-gk [bc-text]
  bc-text)
