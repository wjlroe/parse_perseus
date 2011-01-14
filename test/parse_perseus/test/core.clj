(ns parse_perseus.test.core
  (:use [parse_perseus.core] :reload)
  (:use [clojure.test]))

(deftest odyssey-first-line ;; Test that the first line of the Odyssey encodes correctly...
  (is (= parse_perseus.core/odyssey_first_line_gk (bc-to-gk parse_perseus.core/odyssey_first_line_bc))))
