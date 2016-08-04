(ns brainfuck-clj.ui
  (:require [brainfuck-clj.actions :as a]
            [brainfuck-clj.interpreter :as interpreter])
  (:use [seesaw.core]))

(defn make-ui
  [program-data program-paused]

  (defn on-run [_]
    (a/set-agent program-paused false))

  (defn on-stop [_]
    (a/set-agent program-paused true))

  (defn on-reload [_]
    (if @program-paused
      (do
        (println) ; FIXME: this will be replaced with cleaning the gui buffer.
        (a/setup-program-data program-data (apply str (:instructions @program-data))))
      (alert "The interpreter must be paused to be able to reload the program.")))

  (defn on-instruction [button]
    (dosync
      (->> (config button :text)
           seq
           first
           char
           (alter program-data interpreter/run-given-instruction))))

  (native!)
  (def f
    (frame
      :title "brainfuck-clj"
      :size [640 :by 480]
      :on-close :exit
      :content (vertical-panel
                 :items [(text
                           :text ""
                           :multi-line? true
                           :editable? false)
                         (horizontal-panel
                           :items [(flow-panel
                                     :items [(button :text "+"
                                                     :listen [:action on-instruction])
                                             (button :text "-"
                                                     :listen [:action on-instruction])
                                             (button :text ">"
                                                     :listen [:action on-instruction])
                                             (button :text "<"
                                                     :listen [:action on-instruction])
                                             (button :text "["
                                                     :listen [:action on-instruction])
                                             (button :text "]"
                                                     :listen [:action on-instruction])
                                             (button :text "."
                                                     :listen [:action on-instruction])
                                             (button :text ","
                                                     :listen [:action on-instruction])]
                                     :align :left)
                                   (flow-panel
                                     :items [(button :text "Run"
                                                     :listen [:action on-run])
                                             (button :text "Stop"
                                                     :listen [:action on-stop])
                                             (button :text "Reload"
                                                     :listen [:action on-reload])]
                                     :align :right)])
                         (scrollable
                           (label :text "" :id :cells)
                           :hscroll :always
                           :vscroll :never)
                         (text
                           :text ""
                           :multi-line? true
                           :editable? true)])))

  (-> f pack!  show!)

  (future
    (loop []
      (Thread/sleep 100)
      (let [program-data @program-data
            cells (:cells (:cell-data program-data))]
        (config! (select f [:#cells]) :text (str cells)))
      (recur))))
