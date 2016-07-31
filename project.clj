(defproject brainfuck-clj "0.1.0-SNAPSHOT"
  :description "A Brainfuck interpreter written in Clojure"
  :url "https://github.com/ElGoreLoco/brainfuck-clj"
  :license {:name "MIT License"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot brainfuck-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
