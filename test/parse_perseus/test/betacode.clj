(ns parse_perseus.test.betacode
  (:use [parse_perseus.betacode] :reload)
  (:use clojure.test
	name.choi.joshua.fnparse))

;(deftest odyssey-first-line ;; Test that the first line of the Odyssey encodes correctly...
;  (is (= parse_perseus.core/odyssey_first_line_gk (bc-to-gk parse_perseus.core/odyssey_first_line_bc))))

(deftest bc-string-to-gk-test
  (is (= "αβγ" (parse "abg"))))

(deftest bc-string-upper-gk-test
  (is (= "ΑΒΓ" (parse "*a*b*g"))))

(deftest bc-string-w-comma
  (is (= "αβγ, ΑΒΓ" (parse "abg, *a*b*g"))))

(deftest bc-string-w-final-sigma
  (is (= "αβγς" (parse "abgs"))))

(deftest bc-string-w-sigma
  (is (= "αβσγ" (parse "absg"))))

(deftest bc-string-w-accute
  (is (= "ὰβ" (parse "a\\b"))))

(deftest bc-string-with-breath-a-accute
  (is (= "ἔρι" (parse "e)/ri"))))

(deftest bc-odyssey-1st-line
  (is (= "ἄνδρα μοι ἔννεπε, μοῦσα, πολύτροπον, μάλα πολλὰ"
	 (parse "a)/ndra moi e)/nnepe, mou=sa, polu/tropon, ma/la polla\\"))))

(deftest diacritic-grave
  (is (= (char 0x0300) (rule-match diacritic
				   #(println "FAILED: " %)
				   #(println "LEFTOVER: " %)
				   (struct state-s (str \\))))))

(deftest diacritic-accute
  (is (= (char 0x0301) (rule-match diacritic
				   #(println "FAILED: " %)
				   #(println "LEFTOVER: " %)
				   (struct state-s "/")))))