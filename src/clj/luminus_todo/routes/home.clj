(ns luminus-todo.routes.home
  (:require [luminus-todo.layout :as layout]
            [luminus-todo.db.core :as db]
            [hiccup.core :as hiccup]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn done-changer [done?]
  (if done?
    {:type "submit" :value "Undo"}
    {:type "submit" :value "Complete"}))

(defn todo-strikethrough [todo]
  (if (:done todo)
    [:del (str todo)]
    (str todo)))

(defn list-todos [todo]
  (hiccup/html
    [:li (todo-strikethrough todo)
      [:form {:action (str "/api/update-todo/" (:id todo))
              :method "POST"}
        [:input (done-changer (:done todo))]
        [:input {:type "hidden"
                 :name "done"
                 :value (str (not (:done todo)))}]]
      [:form {:action (str "/api/delete-todo/" (:id todo))
              :method "POST"}
        [:input {:type "submit" :value "Delete"}]]]))

(defn list-todo-lists [lists-and-todos]
  (println lists-and-todos)
  (println (db/get-todos {:id 1}))
  (hiccup/html
    [:h1 (:title (first lists-and-todos))]
    [:ul (map list-todos (db/get-todos {:id (:list lists-and-todos)}))]))

(defn home-page []
  (layout/render "home.html")) ;; for later cljs / re-frame / re-com
  ;(hiccup/html
    ;(list-todo-lists (db/get-lists-joined-todos))
    ;[:form {:action "/api/add-todo" :method "POST"}
          ;[:input {:type "Text" :name "description" :placeholder "Todo: "}]
          ;[:input {:type "submit" :value "Add todo to list"}]]))

(defn about-page []
  (hiccup/html
    [:h1 "This project is a todo list implementation.  For DemocracyWorks!"]))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)))

;; (GET "/docs" [] (response/ok (-> "docs/docs.md" io/resource slurp)))
