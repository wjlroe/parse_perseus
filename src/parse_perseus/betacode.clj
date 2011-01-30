(ns parse_perseus.betacode
  (:use name.choi.joshua.fnparse
	parse_perseus.core))

(defstruct state-s :remainder :column :line)

(def star (lit \*))
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

(def apply-str (partial apply str))

(def beta-string
     (semantics (rep* (alt character anything))
		apply-str))

(defn parse [tokens]
  (rule-match beta-string
	      #(println "FAILED: " %)
	      #(println "LEFTOVER: " %2)
	      (struct state-s tokens 0 0)))

