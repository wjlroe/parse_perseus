(ns parse_perseus.test.book
  (:use [parse_perseus.book] :reload)
  (:use expectations))

(expect seq? (parse-book-xml {:book-xml "something"}))
