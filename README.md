Clojurecom
---------------

&copy; Danny Purcell 2013 | MIT license

Makes creating command line tools as easy as writing a function library.

When a clojure library is run from the terminal and loads Clojurecom, `*command-line-args*` will be parsed for a namespace, command name,
and command arguments. Clojurecom will match the command name to a function in the calling module, and eval the function with the given arguments.
The result will then be printed to the terminal.

Features
---------------

NOTE: This is a very rough first release.

* Proof of concept

Usage
---------------

The project file `test/util_test_lib.clj` demonstrates the general usage pattern for Clojurecom as follows.

```
(ns util-test-lib)

(defn test-command "A simple test command" [msg] (str "Running test-command: msg = " msg))

(defn test-command-no-docs [] "command test")


(load-file "src/clojurecom.clj")

```

This is a first trial example and will most likely change soon. That said, the general pattern will remain the same: write a function library, load Clojurecom at the bottom.

At present the consuming library must be run via `java -cp ./test/lib/clojure.jar clojure.main <file_name> <namespace> <command> [args]`
this will change in the near future as I figure out a nicer ways to run things with Java/Clojure.

