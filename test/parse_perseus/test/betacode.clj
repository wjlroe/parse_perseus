(ns parse_perseus.test.betacode
  (:use [parse_perseus.betacode] :reload)
  (:use clojure.test
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

;; (deftest check-two-as
;;   (is (= "aa" (test-rule two-as "aa"))))

;; (deftest check-almost-full-word
;;   (is (= "aa" (test-rule almost-full-word "aas"))))

;; (deftest bc-any-word
;;   (is (= "ας" (test-rule any-word "as"))))

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
