(ns luminus-todo.routes.home
  (:require [luminus-todo.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  ; (layout/render "home.html")
  (str "<img src='http://www.lfed-mw.com/animations/under_construction_animated.gif'>"))

(defn about-page []
  (str "<h1>This project is a todo list implementation.  For DemocracyWorks!"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page))
  (GET "/docs" [] (response/ok (-> "docs/docs.md" io/resource slurp))))

