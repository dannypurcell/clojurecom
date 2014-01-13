Clojurecom
---------------

&copy; Danny Purcell 2014 | MIT license

Makes creating command line apps as easy as writing a function library.

When a clojure library is run from the terminal and calls Clojurecom, the given args will be parsed for a namespace,
command name, and command arguments. Clojurecom will match the leading arguments to a namespace and function, parse the
remaining arguments for options and call the function with the rest of the arguments and the parsed options.
The result will then be printed to the terminal.

Features
---------------

NOTE: This is still a very early work in progress.

* Proof of concept
* Calls functions in any loaded namespace
* Passes remaining args to called function
* Prints a nicely formatted console message on errors

Still to come
---------------

* Automatically handle options for functions with multiple arg lists
* Get function doc strings to line up nicely in a vertical column

Usage
---------------

* In your project.clj
    * Add `[clojurecom "0.1.0-SNAPSHOT"]` to :dependencies
* In your main namespace
    * Add `(:require [clojurecom.core :as console])` at the top
    * Define your main function `(defn -main [& args] (console/run args))`
* Run
    * In development `lein run [-m qualified_main_fn] <namespace> <command> [args]`
    * Pre-built `java -jar <standalone_jar_path> <namespace> <command> [args]`

Example:

From `test/util_test_lib.clj`
```
(ns util-test-lib
  (:require [clojurecom.core :as console]))

(defn test-command "A simple test command" [msg] (str "Running test-command: msg = " msg))

(defn test-command-no-docs [] "command test")

(defn -main [& args]
  (console/run-and-print args))

```

This is still a work in progress usage and running patterns are subject to change.
That said, the general pattern will remain the same: write a function library, run with clojurecom.

At present the consuming library must be run via
`lein run [-m qualified_main_fn] <namespace> <command> [args]`
or
`java -jar <standalone_jar_path> <namespace> <command> [args]`
this will change in the near future as I figure out a nicer ways to run things with Java/Clojure.

