(ns user
  (:require [mount.core :as mount]
            [luminus-todo.figwheel :refer [start-fw stop-fw cljs]]
            luminus-todo.core))

(defn start []
  (mount/start-without #'luminus-todo.core/repl-server))

(defn stop []
  (mount/stop-except #'luminus-todo.core/repl-server))

(defn restart []
  (stop)
  (start))


