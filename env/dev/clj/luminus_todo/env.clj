(ns luminus-todo.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [luminus-todo.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[luminus-todo started successfully using the development profile]=-"))
   :middleware wrap-dev})
