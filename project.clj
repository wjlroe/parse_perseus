(defproject parse_perseus "1.0.0-SNAPSHOT"
  :description "For parsing Perseus files - http://www.perseus.tufts.edu/hopper/opensource/download"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [factual/fnparse "2.3.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]
                 [ring/ring-json "0.2.0"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [cheshire "5.2.0"]]
  :dev-dependencies [[lein-ring "0.8.3"]
                     [lein-marginalia "0.7.1"]]
  :jvm-opts ["-Dfile.encoding=utf-8"]
  :deview-server 9000
  :main parse_perseus.book)
