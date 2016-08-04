(ns brainfuck-clj.ui
  (:require [brainfuck-clj.actions :as a])
  (:use [seesaw.core]))

(defn make-ui
  [program-data program-paused]
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
                                     :items [(button :text "+")
                                             (button :text "-")
                                             (button :text ">")
                                             (button :text "<")
                                             (button :text "[")
                                             (button :text "]")
                                             (button :text ".")
                                             (button :text ",")]
                                     :align :left)
                                   (flow-panel
                                     :items [(button :text "Run"
                                                     :listen [:action (fn [_] (a/set-agent program-paused false))])
                                             (button :text "Stop"
                                                     :listen [:action (fn [_] (a/set-agent program-paused true))])
                                             (button :text "Reload"
                                                     :listen [:action (fn [_]
                                                                        (if @program-paused
                                                                          (do
                                                                            (println)
                                                                            (a/setup-program-data
                                                                              program-data
                                                                              (apply str (:instructions @program-data))))
                                                                          (alert "The interpreter must be paused to be able to reload the program.")))])]
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
