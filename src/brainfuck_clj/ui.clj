(ns brainfuck-clj.ui
  (:use [seesaw.core]))

(defn make-ui
  [program-data program-terminated]
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
                                     :items [(button :text "Run")
                                             (button :text "Stop")
                                             (button :text "Reload")]
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
