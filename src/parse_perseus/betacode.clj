(ns parse_perseus.betacode
  (:use name.choi.joshua.fnparse
	clojure.pprint
	parse_perseus.core)
  (:import java.text.Normalizer))

(defstruct state-s :remainder)

(def diacritics
     {\\ 0x0300
      \/ 0x0301
      \= 0x0342
      \) 0x0313
      \( 0x0314
      \| 0x0345})

(defn normalize [text]
  (Normalizer/normalize text java.text.Normalizer$Form/NFC))

;(def apply-str (fn[x] (normalize (apply str x))))
(def apply-str (partial apply str))
(def diacritic-char (fn[x] (char (diacritics x))))

(def star (lit \*))
(def diacritic
     (semantics (lit-alt-seq (keys diacritics) lit)
		diacritic-char))

(def lower-char
     (semantics (lit-alt-seq (map :beta beta-map) lit)
		beta-char-to-greek-char))

(def upper-char
     (complex [_ star
	       character lower-char]
	      (- character 32)))

(def character
     (semantics (alt upper-char lower-char)
		char))

(def word
     (rep+ (conc character (rep* diacritic))))

(def beta-string
     (semantics (rep* (alt word anything))
		apply-str))

(defn parse [tokens]
  (rule-match beta-string
	      #(println "FAILED: " %)
	      #(println "LEFTOVER: " %2)
	      (struct state-s tokens)))

