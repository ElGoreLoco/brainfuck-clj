(ns brainfuck-clj.actions
  (:require [brainfuck-clj.interpreter :as interpreter]))

(defn set-agent
  [a v]
  (send a (fn [_] v)))

(defn setup-program-data
  [program-data s]
  (dosync (ref-set program-data (interpreter/str->program-data s))))
