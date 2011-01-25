(ns parse_perseus.test.core
  (:use [parse_perseus.core] :reload)
  (:use [clojure.test]))

;(deftest odyssey-first-line ;; Test that the first line of the Odyssey encodes correctly...
;  (is (= parse_perseus.core/odyssey_first_line_gk (bc-to-gk parse_perseus.core/odyssey_first_line_bc))))

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