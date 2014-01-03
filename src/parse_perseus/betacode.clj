(ns parse_perseus.betacode
  (:use
    name.choi.joshua.fnparse
    clojure.pprint
    [clojure.set :only [select]])
  (:import
    java.text.Normalizer))

(defstruct state-s :remainder)

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

(defn beta-char-to-greek-char [char]
  (-> (select #(= (:beta %) char) beta-map)
      first
      :greek))

(defn bc-string-to-gk [code]
  (apply str (map #(char (beta-char-to-greek-char %)) code)))

;; Need to combine some more specifically rather than relying on normalisation...
;; BUT this won't work with the lit-alt-seq because it assumes char literals as keys
(def diacritics
     {"/+" 0x0385
      \\ 0x0300
      \/ 0x0301
      \= 0x0342
      \) 0x0313
      \( 0x0314
      \| 0x0345
      \+ 0x0308})

(defn normalize [text]
  (Normalizer/normalize text java.text.Normalizer$Form/NFC))

(def normalize-apply-str (fn[x] (normalize (apply str x))))
(def apply-str (partial apply str))
(def diacritic-char (fn[x] (char (diacritics x))))

(defn rep*? [subrule]
  (semantics (rep* (invisi-conc subrule (followed-by subrule)))
             apply-str))

(def star (lit \*))
(def iota-dialytika-tonos
  (constant-semantics (lit-conc-seq "i/+" lit)
                      (char 0x0390)))
(def diacritic
  (semantics (lit-alt-seq (keys diacritics) lit)
             diacritic-char))

(def diacritic-chars
  (rep* diacritic))

(def basic-char
  (semantics (lit-alt-seq (map :beta beta-map) lit)
             beta-char-to-greek-char))

(def lower-char
  (complex [basic basic-char]
           (char basic)))

(def upper-char
  (complex [_ star
            diacritics diacritic-chars
            character basic-char]
           (str (char (- character 32)) (apply str diacritics))))

(def final-sigma
  (constant-semantics (lit \s)
                      (str (char (- (beta-char-to-greek-char \s) 1)))))

(def character (alt iota-dialytika-tonos upper-char lower-char))

(def char-form
  (complex [char character
            diacritics diacritic-chars]
           (normalize-apply-str (str char (apply str diacritics)))))

(def non-greedy-word
  (semantics (rep*? char-form)
             apply-str))

(def any-character
  (semantics (rep* char-form)
             apply-str))

(def any-word
  (semantics (conc char-form any-character)
             apply-str))

(def word-w-final-sigma
  (semantics (conc non-greedy-word final-sigma)
             apply-str))

(def word
  (semantics (alt word-w-final-sigma
                  any-word)
             apply-str))

(def beta-string
  (semantics (rep* (alt word anything))
             apply-str))

(defn parse-bc [tokens]
  (rule-match beta-string
              #(println "FAILED: " %)
              #(println "LEFTOVER: " %2)
              (struct state-s tokens)))
