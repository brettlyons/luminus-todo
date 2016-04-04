(ns luminus-todo.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [luminus-todo.layout :refer [error-page]]
            [luminus-todo.routes.home :refer [home-routes]]
            [luminus-todo.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [luminus-todo.middleware :as middleware]))

(def app-routes
  (routes
    #'service-routes
    (wrap-routes #'home-routes middleware/wrap-csrf)
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))

(def app (middleware/wrap-base #'app-routes))
