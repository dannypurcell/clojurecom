(ns util-test-lib)

(defn test-command "A simple test command" [msg] (str "Running test-command: msg = " msg))

(defn test-command-no-docs [] "command test")


(load-file "src/core.clj")

