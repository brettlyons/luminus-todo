(ns luminus-todo.routes.home
  (:require [luminus-todo.layout :as layout]
            [luminus-todo.db.core :as db]
            [hiccup.core :as hiccup]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))


(defn list-todos [todo]
  (hiccup/html
    [:li (str todo)]))

(defn home-page []
  ; (layout/render "home.html"))
  (hiccup/html
    [:ul (map list-todos (db/get-todos))]))


(defn about-page []
  (hiccup/html
    [:h1 "This project is a todo list implementation.  For DemocracyWorks!"]))

(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/about" [] (about-page))
  (GET "/docs" [] (response/ok (-> "docs/docs.md" io/resource slurp))))

