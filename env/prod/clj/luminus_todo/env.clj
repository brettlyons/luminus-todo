(ns luminus-todo.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[luminus-todo started successfully]=-"))
   :middleware identity})
