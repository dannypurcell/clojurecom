(ns clojurecom.options
  (:import (clojure.lang PersistentArrayMap))
  (:require [clojure.string :as cs]))

(defn get-long-key [opt]
  (str "--" (cond
              (= PersistentArrayMap (type opt)) "map"
              :else opt)))

(defn get-long-val [opt]
  (cs/upper-case (str (cond
                        (= PersistentArrayMap (type opt)) "map"
                        :else opt))))

(defn get-long-opt-spec [opt]
  (str (get-long-key opt) " " (get-long-val opt)))

(defn to-option-spec [opt] [(get-long-opt-spec opt)])

(defn make-option-specs [optional-args]
  (try
    (cond
      (vector? optional-args) (vec (map to-option-spec optional-args))
      (seq? optional-args) (vec (map make-option-specs optional-args))
      (nil? optional-args) []
      :else (make-option-specs [optional-args]))
    (catch UnsupportedOperationException ex [(to-option-spec optional-args)])))

; TODO remove
(:arglists (meta (resolve 'test-fn)))
(make-option-specs (:arglists (meta (resolve 'test-fn))))
