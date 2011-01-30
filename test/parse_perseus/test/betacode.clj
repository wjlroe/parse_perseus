(ns parse_perseus.test.betacode
  (:use [parse_perseus.betacode] :reload)
  (:use [clojure.test]))

;(deftest odyssey-first-line ;; Test that the first line of the Odyssey encodes correctly...
;  (is (= parse_perseus.core/odyssey_first_line_gk (bc-to-gk parse_perseus.core/odyssey_first_line_bc))))

(deftest bc-string-to-gk-test
  (is (= "αβγ" (parse "abg"))))

(deftest bc-string-upper-gk-test
  (is (= "ΑΒΓ" (parse "*a*b*g"))))

(deftest bc-string-w-comma
  (is (= "αβγ, ΑΒΓ" (parse "abg, *a*b*g"))))
