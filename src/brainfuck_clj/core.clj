(ns brainfuck-clj.core
  (:require [clojure.set])
  (:gen-class))

;;;;

(def program-data-ref (ref nil))
(def program-terminated (agent false))

;;;; Cell data

(defn create-cell-data
  "Returns default/empty cell data."
  []
  {:cells [0]
   :data-pointer 0})

(defn new-cell
  "Add a cell to the end."
  [cell-data]
  (merge cell-data {:cells (conj (:cells cell-data) 0)}))

(defn edit-cell
  "Change the value of the cell at index."
  [cell-data index value]
  (let [{:keys [cells data-pointer]} cell-data]
    (merge cell-data {:cells (assoc cells index value)})))

(defn get-cell-value
  "Return value of the current cell."
  [{:keys [cells data-pointer]}]
  (get cells data-pointer))

(defn move-right
  [cell-data]
  (if (get (:cells cell-data) (inc (:data-pointer cell-data)))
    (merge cell-data {:data-pointer (inc (:data-pointer cell-data))})
    (move-right (new-cell cell-data))))

(defn move-left
  [cell-data]
  (if (> (:data-pointer cell-data) 0)
    (merge cell-data {:data-pointer (dec (:data-pointer cell-data))})
    cell-data))

(defn- fn-cell
  [cell-data f]
  (let [cells (:cells cell-data)
        data-pointer (:data-pointer cell-data)
        result (f (get cells data-pointer))]
    (edit-cell cell-data data-pointer result)))

(defn inc-cell
  [cell-data]
  (fn-cell cell-data inc))

(defn dec-cell
  [cell-data]
  (fn-cell cell-data dec))

(defn output-cell
  "Print the character stored in the current cell."
  [cell-data]
  (let [cells (:cells cell-data)
        data-pointer (:data-pointer cell-data)]
    (print (char (get cells data-pointer)))
    (flush) ; If the program doesn't flush the output stream, characters will be displayed every once in a while.
    cell-data))

(defn- str->number
  "Take the first character of the string and convert it to a number."
  [s]
  (long (first s)))

(defn input-cell
  "Take input from the user and store it in the current cell."
  [cell-data]
  (let [{:keys [cells data-pointer]} cell-data
        input (str->number (read-line))]
    (if (number? input)
      (edit-cell cell-data data-pointer input))))

;;;; Program data

(defn move-instruction-pointer
  [program-data index]
  (merge program-data {:instruction-pointer index}))

(defn inc-instruction-pointer
  [program-data]
  (let [new-instruction-pointer (inc (:instruction-pointer program-data))]
    (move-instruction-pointer program-data new-instruction-pointer)))

(defn edit-cell-data
  [program-data f]
  (merge program-data {:cell-data (f (:cell-data program-data))}))

(defn run-instruction
  [program-data]
  (let [{:keys [instructions instruction-pointer loops cell-data]} program-data
        current-instruction (nth instructions instruction-pointer)]
    (inc-instruction-pointer
      (case current-instruction
        \+ (edit-cell-data program-data inc-cell)
        \- (edit-cell-data program-data dec-cell)
        \> (edit-cell-data program-data move-right)
        \< (edit-cell-data program-data move-left)
        \[ (if (= (get-cell-value cell-data) 0)
             (move-instruction-pointer
               program-data
               (get loops instruction-pointer))
             program-data)
        \] (if (not= (get-cell-value cell-data) 0)
             (move-instruction-pointer
               program-data
               (get (clojure.set/map-invert loops) instruction-pointer))
             program-data)
        \. (do (output-cell cell-data) program-data)
        \, (merge program-data {:cell-data (input-cell cell-data)})
        program-data))))

(defn run-all-instructions
  []
  (println "Started execution")
  (loop []
    (dosync
      (if (< (:instruction-pointer @program-data-ref) (count (:instructions @program-data-ref)))
        (alter program-data-ref run-instruction)
        (send program-terminated (fn [a] true))))
    (if (not @program-terminated)
      (recur))))

;;;; Loops

(defn last-empty-loop
  "Returns the last bracket that hasn't been matched with another yet.
  If all of the loops are closed, return -1."
  [loops]
  (reduce (fn [n1 [n2 value]]
            (if (nil? value)
              (max n1 n2)
              n1))
          -1
          loops))

(defn generate-loops
  "Return map with keys as the opening brackets and values as the closing
  brackets."
  [instructions]
  (reduce (fn [m i]
            (case (nth instructions i)
              \[ (assoc m i nil)
              \] (merge m {(last-empty-loop m) i})
              m))
          {}
          (range (count instructions))))

;;;; String manipulation

(defn valid-command?
  [command]
  (reduce #(if (true? %1) true (= command %2))
          false
          [\> \< \+ \- \[ \] \. \,]))

(defn str->instructions
  "Takes a string and returns a list with all of the valid characters."
  [s]
  (vec (filter valid-command? (seq s))))

(defn str->program-data
  [s]
  (let [instructions (str->instructions s)
        loops (generate-loops instructions)]
    {:instructions instructions
     :instruction-pointer 0
     :loops loops
     :cell-data (create-cell-data)}))

;;;; Main

(defn -main
  [& args]
  (if (= 1 (count args))
    (do
      ;; Setup program-data
      (dosync (ref-set program-data-ref (str->program-data (slurp (first args)))))

      ;; Print the cells state every 0.5s
      (future
        (dosync
          (println (:cells (:cell-data @program-data))))
        (when (not @program-terminated)
          (Thread/sleep 500)
          (recur)))

      ;; Begin interpreter
      (run-all-instructions))
    (println "You should specify a filepath."))
  (shutdown-agents))
