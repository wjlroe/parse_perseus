(ns parse_perseus.test.betacode
  (:use [parse_perseus.betacode] :reload)
  (:use clojure.test
	name.choi.joshua.fnparse))

(defn test-rule [rule string]
  (rule-match rule
	      #(println "FAILED: " %)
	      #(println "LEFTOVER: " %)
	      (struct state-s string)))

;(deftest odyssey-first-line ;; Test that the first line of the Odyssey encodes correctly...
;  (is (= parse_perseus.core/odyssey_first_line_gk (bc-to-gk parse_perseus.core/odyssey_first_line_bc))))

(deftest bc-string-to-gk-test
  (is (= "αβγ" (parse-bc "abg"))))

(deftest bc-string-upper-gk-test
  (is (= "ΑΒΓ" (parse-bc "*a*b*g"))))

(deftest bc-string-w-comma
  (is (= "αβγ, ΑΒΓ" (parse-bc "abg, *a*b*g"))))

(deftest bc-string-w-final-sigma
  (is (= "αβγς" (parse-bc "abgs"))))

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
       "to\\n d' a)pameibo/menos prose/fh polu/mhtis *)odusseu/s:" "τὸν δ᾽ ἀπαμειβόμενος προσέφη πολύμητις Ὀδυσσεύς:"))

(deftest upper-char-test
  (are [bc greek] (= greek (test-rule upper-char bc))
       "*o" "Ο"
       "*)o" (str (char 0x1f48))))

(deftest word-match
  (are [bc greek] (= greek (test-rule word bc))
       "a\\b" "ὰβ"
       "absg" "αβσγ"))

(deftest diacritic-grave
  (is (= (char 0x0300) (test-rule diacritic (str \\)))))

(deftest diacritic-accute
  (is (= (char 0x0301) (test-rule diacritic "/"))))