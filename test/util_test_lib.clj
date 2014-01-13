(ns util-test-lib
  (:require [clojurecom.core :as console]))

(defn test-command "A simple test command" [msg] (str "Running test-command: msg = " msg))

(defn test-command-no-docs [] "command test")

(defn -main [& args]
  (console/run-and-print args))
