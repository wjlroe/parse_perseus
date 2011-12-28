(defproject parse_perseus "1.0.0-SNAPSHOT"
  :description "For parsing Perseus files - http://www.perseus.tufts.edu/hopper/opensource/download"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [fnparse "2.2.7"]
                 [compojure "1.0.0-RC2"]
                 [hiccup "0.3.7"]
                 [ring-json-params "0.1.3"]
                 [ring/ring-jetty-adapter "1.0.1"]
                 [clj-json "0.4.3"]
                 [sandbar/sandbar-session "0.2.5"]]
  :dev-dependencies [[swank-clojure "1.3.4-SNAPSHOT"]
		     [deview/lein-deview "1.0.5"]
                     [lein-ring "0.6.0-SNAPSHOT"]
                     [lein-marginalia "0.7.0-SNAPSHOT"]]
  :jvm-opts ["-Dfile.encoding=utf-8"]
  :deview-server 9000
  :main parse_perseus.book)
