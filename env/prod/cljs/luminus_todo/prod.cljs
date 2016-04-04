(ns luminus-todo.app
  (:require [luminus-todo.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
