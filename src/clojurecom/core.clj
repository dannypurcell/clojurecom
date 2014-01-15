(ns clojurecom.core
  (:import (java.io StringWriter PrintWriter)
           (clojure.lang PersistentArrayMap))
  (:require [clojure.string :as cs]
            [clojure.tools.cli :as cli]
            [clojurecom.options :as opts]))

(defn ns-by-name
  ([] (ns-by-name nil))
  ([allowed-ns]
   (reduce merge (map (fn [ns-ref] {(cs/replace (str ns-ref) "." " ") ns-ref})
                      (let [all (all-ns)]
                        (if allowed-ns
                          (filter (fn [ns-obj] (some #{(str ns-obj)} allowed-ns)) all)
                          all))))))

(defn lookup-ns [ns-by-name args]
  (reduce
    (fn [acc arg]
      (if (= PersistentArrayMap (type acc))
        acc
        (let [conjoined (conj acc arg)
              ns-candidate (cs/join " " conjoined)
              ns-obj (get ns-by-name ns-candidate)]
          (if (nil? ns-obj)
            conjoined
            {:consumed-args conjoined
             :matched-ns    ns-obj}))))
    []
    args))

(defn map-ns-functions [ns-obj]
  (if ns-obj
    (reduce merge
            (map (fn [fn-meta]
                   (let [fn-sym (first fn-meta)
                         meta-map (meta (second fn-meta))
                         fn-map {:fn (resolve (symbol (str ns-obj "/" fn-sym)))}]
                     {(str fn-sym) (merge fn-map meta-map)}))
                 (ns-publics ns-obj)))
    nil))

(defn filter-args [cmd-words args]
  (if (and cmd-words args)
    (subvec (vec args) (count cmd-words))
    args))

(defn parse-arglists [arglists]
  (if (= (count arglists) 1)
    {:required (first arglists)}
    (let [list-count (count arglists)
          counted-args (frequencies (reduce (fn [acc nlist] (into acc nlist)) [] arglists))]
      (reduce (fn [acc arg-count-pair] (if (= (second arg-count-pair) list-count)
                                         (merge-with concat {:required (first arg-count-pair)} acc)
                                         (merge-with concat {:optional (first arg-count-pair)} acc)))
              {} counted-args))))

(defn make-fn-args [arglists args opts]
  ;TODO
  (let [{required-args :required allowed-opts :optional} (parse-arglists arglists)]
    args))

(defn layout
  ([{msg :msg usage :usage commands :cmds opt-summary :opts ex :ex}]
   (str (or msg ex) "\n"
        "Usage: " usage "\n"
        (if (and commands (> (count commands) 0))
          (str "\n"
               "Commands:\n"
               (cs/join
                 (map (fn [cmd-map]
                        (let [cmd (first cmd-map)
                              doc (:doc (second cmd-map))]
                          (if doc (format "%-20s - %-75s\n" cmd doc) (format "%-20s\n" cmd))))
                      commands)))
          "")
        (if opt-summary
          (str "\nOptions:\n" opt-summary)
          "")
        (if ex
          (let [sw (StringWriter.)
                pw (PrintWriter. sw)]
            (.printStackTrace ex pw)
            (str "\nTrace:\n" sw))))))

(defn run-command
  "Runs :fn from the given cmd-map with the given args and opts. Returns the result of the function call.
   If an exception occurs, a formated output string composed of :name and :arglists from cmd-map will be
   returned instead."
  [cmd-map args]
  (let [parsed-arglist-map (parse-arglists (:arglists cmd-map))
        required-args (:required parsed-arglist-map)
        opt-specs (opts/make-option-specs (:optional parsed-arglist-map))
        usage (str (:name cmd-map) " " (:arglists cmd-map))
        {parsed-opts :options remaining-args :arguments summary :summary errors :errors} (cli/parse-opts args opt-specs)
        fn-args (make-fn-args (:arglists cmd-map) remaining-args parsed-opts)]
    (try
      (apply (:fn cmd-map) fn-args)
      (catch Exception ex
        (layout {:usage usage :ex ex :opts summary})))))

(defn run
  "Parses args to find and run a command. If cmd-namespaces are given then only commands in those namespaces will be
  considered when looking up commands. Otherwise all loaded namespaces are considered."
  ([args] (run args nil))
  ([args cmd-namespaces]
   (let [selected-ns-map (lookup-ns (ns-by-name cmd-namespaces) args)
         availiable-cmds (map-ns-functions (:matched-ns selected-ns-map))
         remaining-args (filter-args (:consumed-args selected-ns-map) args)
         cmd-map (get availiable-cmds (first remaining-args))
         usage "command <args> [options]"
         cmd-args (rest remaining-args)]
     (cond
       (or (= 0 (count args)) (= 0 (count remaining-args))) (layout {:msg "No command specified." :usage usage :cmds availiable-cmds})
       (= nil cmd-map) (layout {:msg "Unrecognized command." :usage usage :cmds availiable-cmds})
       :else (try
               (run-command cmd-map cmd-args)
               (catch Exception ex
                 (layout {:usage usage :cmds availiable-cmds :ex ex})))))))

(defn run-and-print [args]
  (let [result (run args)]
    (if-not (nil? result)
      (println result))))

(defn -main [& args]
  (run-and-print args))

; TODO remove
(defn test-fn
  ([arg1] (str arg1))
  ([arg1 arg2] (str arg1 " " arg2))
  ([{arg1 :first arg2 :second} arg3 arg4] (str arg1 " " arg2)))
; TODO remove
(run ["clojurecom" "core" "test-fn" "cat"])