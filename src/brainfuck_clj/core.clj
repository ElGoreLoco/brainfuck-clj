(ns brainfuck-clj.core
  (:require [brainfuck-clj.interpreter :as interpreter])
  (:gen-class))

;;;; Mutable data

(def program-data (ref nil))
(def program-terminated (agent false))

;;;; Main

(defn -main
  [& args]
  (if (= 1 (count args))
    (do
      ;; Setup program-data
      (dosync (ref-set program-data
                       (interpreter/str->program-data (slurp (first args)))))

      ;; Print the cells state every 0.5s
      (future
        (dosync
          (println (:cells (:cell-data @program-data))))
        (when (not @program-terminated)
          (Thread/sleep 500)
          (recur)))

      ;; Begin interpreter
      (interpreter/run-all-instructions program-data program-terminated))
    (println "You should specify a filepath."))
  (shutdown-agents))
