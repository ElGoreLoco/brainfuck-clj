(ns brainfuck-clj.actions)

(defn set-agent
  [a v]
  (send a (fn [_] v)))
