(ns clojurecom.core
  (:import (java.io StringWriter PrintWriter)
           (clojure.lang PersistentArrayMap)))

(defn ns-by-name
  ([] (ns-by-name nil))
  ([allowed-ns]
   (reduce merge (map (fn [ns-ref] {(clojure.string/replace (str ns-ref) "." " ") ns-ref})
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
              ns-candidate (clojure.string/join " " conjoined)
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

(defn layout
  ([msg usage commands] (layout msg usage commands nil))
  ([msg usage commands opt-summary] (layout msg usage commands opt-summary {} nil))
  ([msg usage commands opt-summary opts ex]
   (str msg "\n"
        "Usage: " usage "\n"
        (if (and commands (> (count commands) 0))
          (str "\n"
               "Commands:\n"
               (clojure.string/join
                 (map (fn [cmd-map] (format "%-15s - %-75s\n" (first cmd-map) (:doc (second cmd-map))))
                      commands)))
          "")
        (if opt-summary
          (str "\nOptions:\n" opt-summary)
          "")
        (if (and opts (:verbose opts) ex)
          (let [sw (StringWriter.)
                pw (PrintWriter. sw)]
            (.printStackTrace ex pw)
            (str "\nTrace:\n" sw))))))

(defn run-command
  "Runs :fn from the given cmd-map with the given args and opts. Returns the result of the function call.
   If an exception occurs, a formated output string composed of :name and :arglists from cmd-map will be
   returned instead."
  [cmd-map args opts]
  (try
    ;TODO check number of args and handle opts as named parameters
    (apply (:fn cmd-map) args)
    (catch Exception ex
      (layout ex (str (:name cmd-map) " " (:arglists cmd-map)) nil nil opts ex))))

(defn run
  "Parses args to find and run a command. If cmd-namespaces are given then only commands in those namespaces will be
  considered when looking up commands. Otherwise all loaded namespaces are considered."
  ([args] (run args nil))
  ([args cmd-namespaces]
   (let [selected-ns-map (lookup-ns (ns-by-name cmd-namespaces) args)
         availiable-cmds (map-ns-functions (:matched-ns selected-ns-map))
         remaining-args (filter-args (:consumed-args selected-ns-map) args)
         cmd-map (get availiable-cmds (first remaining-args))
         usage "<command> [args] [options]"
         cmd-args (rest remaining-args)
         opts {:verbose true}]
     (cond
       (or (= 0 (count args)) (= 0 (count remaining-args))) (println (layout "No command specified." usage availiable-cmds))
       (= nil cmd-map) (println (layout "Unrecognized command." usage availiable-cmds))
       :else (try
               (run-command cmd-map cmd-args opts)
               (catch Exception ex
                 (println (layout ex usage availiable-cmds nil opts ex))))))))

(defn run-and-print [args]
  (println (run args)))

(defn -main [& args]
  (run-and-print args))
