(ns brainfuck-clj.core
  (:require [clojure.set])
  (:gen-class))

;;;; Handle cells

(defn create-cell-data []
  "Returns default/empty cell data."
  {:cells [0]
   :data-pointer 0})

(defn new-cell
  "Add a cell to the end."
  [cells]
  (merge cells {:cells (conj (:cells cells) 0)}))

(defn edit-cell
  "Change the value of the cell at index."
  [cell-data index value]
  (let [cells (:cells cell-data)
        data-pointer (:data-pointer cell-data)]
    (merge cell-data {:cells (assoc cells index value)})))

(defn get-cell-value
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
    (edit-cell cell-data data-pointer result)
    #_(merge cell-data {:cells (assoc cells data-pointer result)})))

(defn inc-cell
  [cell-data]
  (fn-cell cell-data inc))

(defn dec-cell
  [cell-data]
  (fn-cell cell-data dec))

(defn output-cell
  [cell-data]
  (let [cells (:cells cell-data)
        data-pointer (:data-pointer cell-data)]
    (print (char (get cells data-pointer)))
    cell-data))

(defn- str->number
  "Take the first character of the string and convert it to a number."
  [s]
  (long (first s)))

(defn input-cell
  [cell-data]
  (let [cells (:cells cell-data)
        data-pointer (:data-pointer cell-data)
        input (str->number (read-line))]
    (if (number? input)
      (edit-cell cell-data data-pointer input))))

;;;; Handle program execution

(defn create-program-data []
  {:instructions []
   :instruction-pointer 0
   :loops {}}) ; Each time the interpreter finds a loop it adds its
               ; position to this vector. When a loop ends, it removes it.

(defn move-instruction-pointer
  [program-data index]
  (merge program-data {:instruction-pointer index}))

(defn inc-instruction-pointer
  [program-data]
  (let [new-instruction-pointer (inc (:instruction-pointer program-data))]
    (move-instruction-pointer program-data new-instruction-pointer)))

;;;; Lexing (?) (should it be called lexing?): transform a string to an instruction vector

(def valid-commands [\> \< \+ \- \[ \] \. \,])

(defn valid-command?
  [command]
  (reduce #(if (true? %1) true (= command %2))
          false
          valid-commands))

(defn str->instructions
  "Takes a string and returns a list with all of the valid characters."
  [s]
  (filter valid-command? (seq s)))

(defn last-empty-loop
  "Returns the last bracket that hasn't been matched with another yet."
  [loops]
  (reduce (fn [n1 [n2 value]]
            (if (nil? value)
              (max n1 n2)
              n1))
          0
          loops))

(defn generate-loops
  [instructions]
  (reduce (fn [m i]
            (if (= (nth instructions i) \[)
              (assoc m i nil)
              (if (= (nth instructions i) \])
                (merge m {(last-empty-loop m) i})
                m)))
          {}
          (range (count instructions))))

(defn str->program-data
  [s]
  (let [instructions (str->instructions s)
        loops (generate-loops instructions)]
    {:instructions instructions
     :instruction-pointer 0
     :loops loops
     :cell-data (create-cell-data)}))

(defn run-instruction
  [program-data]
  (let [{:keys [instructions instruction-pointer loops cell-data]} program-data
        current-instruction (nth instructions instruction-pointer)]
    ;(println instruction-pointer)
    (inc-instruction-pointer
      (cond
        (= current-instruction \+) (merge program-data {:cell-data (inc-cell cell-data)})
        (= current-instruction \-) (merge program-data {:cell-data (dec-cell cell-data)})
        (= current-instruction \>) (merge program-data {:cell-data (move-right cell-data)})
        (= current-instruction \<) (merge program-data {:cell-data (move-left cell-data)})
        (= current-instruction \[) (if (= (get-cell-value cell-data) 0)
                                     (move-instruction-pointer program-data (get loops instruction-pointer))
                                     program-data)
        (= current-instruction \]) (if (not= (get-cell-value cell-data) 0)
                                     (move-instruction-pointer program-data (get (clojure.set/map-invert loops) instruction-pointer))
                                     program-data)
        (= current-instruction \.) (do (output-cell cell-data) program-data)
        (= current-instruction \,) (merge program-data {:cell-data (input-cell cell-data)})
        :else program-data))))

(defn run-all-instructions
  [program-data]
  (loop [program-data program-data]
    (if (< (:instruction-pointer program-data) (count (:instructions program-data)))
      (recur (run-instruction program-data))
      program-data)))

(defn -main
  [& args]
  (doall
    (map #(run-all-instructions (str->program-data (slurp %))) args)))
