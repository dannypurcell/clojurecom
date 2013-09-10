(ns clojurecom.core)

(defn run-command
  "Runs a the named command with the given args"
  [args]
  (if (= 0 (count args))
        "No command specified"
        (let [ command (symbol (str (first args) "/" (nth args 1)))
               arguments (apply str (map #(str " " "\""%"\"") (nthrest args 2)))
               cmd-str (str "(" command arguments ")")]
          (eval (read-string cmd-str))))
  )

(println (run-command *command-line-args*))
