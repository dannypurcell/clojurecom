(defproject clojurecom "0.1.0-SNAPSHOT"
  :description "Makes creating command line apps as easy as writing a function library."
  :url "http://github.com/dannypurcell/clojurecom"
  :license {:name "MIT License"
            :url  "http://github.com/dannypurcell/clojurecom/blob/master/LICENSE"}
  :main clojurecom.core
  :aot [clojurecom.core]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.3.1"]])

