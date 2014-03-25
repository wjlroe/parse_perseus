(defproject parse_perseus "2.0.0-SNAPSHOT"
  :description "For parsing Perseus files - http://www.perseus.tufts.edu/hopper/opensource/download"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [factual/fnparse "2.3.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.clojars.wjlroe/hiccup "1.0.5-SNAPSHOT"]
                 [environ "0.4.0"]
                 [selmer "0.5.9"]]
  :profiles {:dev {:dependencies [[expectations "1.4.56"]]}}
  :plugins [[lein-expectations "0.0.8"]]
  :jvm-opts ["-Dfile.encoding=utf-8"]
  :main parse_perseus.book)
