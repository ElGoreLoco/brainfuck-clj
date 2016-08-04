(ns brainfuck-clj.core
  (:require [brainfuck-clj.interpreter :as interpreter]
            [brainfuck-clj.ui :as ui])
  (:gen-class))

;;;; Mutable data

(def program-data (ref nil))
(def program-paused (agent false))

;;;; Main

(defn -main
  [& args]
  (if (= 1 (count args))
    (do
      ;; Setup program-data
      (dosync (ref-set program-data
                       (interpreter/str->program-data (slurp (first args)))))

      (ui/make-ui program-data program-paused)

      ;; Begin interpreter
      (interpreter/run-all-instructions program-data program-paused))
    (println "You should specify a filepath."))
  (shutdown-agents))
