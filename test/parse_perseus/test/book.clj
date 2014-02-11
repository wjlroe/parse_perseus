(ns parse_perseus.test.book
  (:use [parse_perseus.book] :reload)
  (:use expectations
        parse_perseus.test.test_helper)
  (:require [clojure.java.io :as io]))

(expect seq? (parse-book-xml (book)))
