(ns brainfuck-clj.core
  (:require [brainfuck-clj.interpreter :as interpreter]
            [brainfuck-clj.ui :as ui]
            [brainfuck-clj.actions :as a])
  (:gen-class))

;;;; Mutable data

(def program-data (ref nil))
(def program-paused (agent true))

;;;; Main

(defn -main
  [& args]
  (if (= 1 (count args))
    (do
      (a/setup-program-data program-data (slurp (first args)))
      (ui/make-ui program-data program-paused)
      (interpreter/run-all-instructions program-data program-paused))
    (println "You should specify a filepath."))
  (shutdown-agents))
