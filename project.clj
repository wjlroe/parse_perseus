(defproject parse_perseus "1.0.0-SNAPSHOT"
  :description "For parsing Perseus files - http://www.perseus.tufts.edu/hopper/opensource/download"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [factual/fnparse "2.3.0"]
                 [org.clojure/data.xml "0.0.7"]
                 [compojure "1.1.5"]
                 [org.clojars.wjlroe/hiccup "1.0.5-SNAPSHOT"]
                 [ring/ring-json "0.2.0"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [cheshire "5.2.0"]
                 [environ "0.4.0"]]
  :profiles {:dev {:dependencies [[expectations "1.4.52"]]}}
  :jvm-opts ["-Dfile.encoding=utf-8"]
  :deview-server 9000
  :main parse_perseus.book)
