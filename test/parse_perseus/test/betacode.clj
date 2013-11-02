(ns parse_perseus.test.betacode
  (:use [parse_perseus.betacode] :reload)
  (:use
    clojure.test
    name.choi.joshua.fnparse))

(defn test-rule [rule string]
  (rule-match rule
	      #(println "FAILED: " %)
	      #(println "STATE: " %1 " LEFTOVER: " %2)
	      (struct state-s string)))

(deftest final-sigma-only
  (is (= "ς" (test-rule final-sigma "s"))))

(deftest not-final-sigma
  (is (= nil (test-rule final-sigma "a"))))

(deftest bc-string-to-gk-test
  (is (= "αβγ" (parse-bc "abg"))))

(deftest bc-string-upper-gk-test
  (is (= "ΑΒΓ" (parse-bc "*a*b*g"))))

(deftest bc-string-w-comma
  (is (= "αβγ, ΑΒΓ" (parse-bc "abg, *a*b*g"))))

(deftest bc-string-w-final-sigma
  (are [bc greek] (= greek (parse-bc bc))
       "abgs" "αβγς"
       "abgs " "αβγς "
       "abgs:" "αβγς:"))

(deftest bc-word-w-final-sigma
  (is (= "αας" (test-rule word-w-final-sigma "aas"))))

(deftest bc-word-without-final-sigma
  (is (= "αα" (test-rule any-word "aa"))))

(deftest bc-string-w-sigma
  (is (= "αβσγ" (parse-bc "absg"))))

(deftest bc-string-w-accute
  (is (= "ὰβ" (parse-bc "a\\b"))))

(deftest bc-string-with-breath-a-accute
  (is (= "ἔρι" (parse-bc "e)/ri"))))

(deftest odyssey-lines
  (are [bc greek] (= greek (parse-bc bc))
       "a)/ndra moi e)/nnepe, mou=sa, polu/tropon, ma/la polla\\" "ἄνδρα μοι ἔννεπε, μοῦσα, πολύτροπον, μάλα πολλὰ"
       "pla/gxqh, e)pei\\ *troi/hs i(ero\\n ptoli/eqron e)/persen" "πλάγχθη, ἐπεὶ Τροίης ἱερὸν πτολίεθρον ἔπερσεν"
       "a)ll' a)/ge, *faih/kwn bhta/rmones o(/ssoi a)/ristoi," "ἀλλ᾽ ἄγε, Φαιήκων βητάρμονες ὅσσοι ἄριστοι,"
       "to\\n d' a)pameibo/menos prose/fh polu/mhtis *)odusseu/s:" "τὸν δ᾽ ἀπαμειβόμενος προσέφη πολύμητις Ὀδυσσεύς:"
       "w(s kai\\ nu=n *ai)/gisqos u(pe\\r mo/ron *)atrei/+dao" "ὡς καὶ νῦν Αἴγισθος ὑπὲρ μόρον Ἀτρεΐδαο"))

(deftest real-apostrophe
  (are [bc greek] (= greek (test-rule word bc))
       "'" "᾽"))

(deftest upper-char-test
  (are [bc greek] (= greek (test-rule upper-char bc))
       "*o" "Ο"))

(deftest word-match
  (are [bc greek] (= greek (test-rule word bc))
       "a\\b" "ὰβ"
       "absg" "αβσγ"
       "i/+" "ΐ"
       "i+" "ϊ"
       "*)o" "Ὀ"
       "as" "ας"))

(deftest diacritic-grave
  (is (= (char 0x0300) (test-rule diacritic (str \\)))))

(deftest diacritic-accute
  (is (= (char 0x0301) (test-rule diacritic "/"))))

(deftest test-final-sigma
  (are [bc greek] (= greek (test-rule final-sigma bc))
       "s" "ς"
       "a" nil))

(deftest beta-to-gk-char-test
  (are [bc gk] (= gk (beta-char-to-greek-char bc))
       \a 0x03b1
       \b 0x03b2
       \g 0x03b3
       \d 0x03b4
       \e 0x03b5
       \z 0x03b6
       \h 0x03b7
       \q 0x03b8
       \i 0x03b9
       \k 0x03ba
       \l 0x03bb
       \m 0x03bc
       \n 0x03bd
       \c 0x03be
       \o 0x03bf
       \p 0x03c0
       \r 0x03c1
       \s 0x03c3
       \t 0x03c4
       \u 0x03c5
       \f 0x03c6
       \x 0x03c7
       \y 0x03c8
       \w 0x03c9))

(deftest bc-string-to-gk-test
  (is (= "αβγ" (bc-string-to-gk "abg"))))

(deftest non-bc-char-test
  (are [non-bc] (= nil (beta-char-to-greek-char non-bc))
       \(
       \)
       \*))
