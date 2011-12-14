(defproject parse_perseus "1.0.0-SNAPSHOT"
  :description "For parsing Perseus files - http://www.perseus.tufts.edu/hopper/opensource/download"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [fnparse "2.2.7"]]
  :dev-dependencies [[swank-clojure "1.3.4-SNAPSHOT"]
		     [deview/lein-deview "1.0.5"]]
  :jvm-opts ["-Dfile.encoding=utf-8"]
  :deview-server 9000
  :main parse_perseus.book)
