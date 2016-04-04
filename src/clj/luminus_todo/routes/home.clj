(ns luminus-todo.routes.home
  (:require [luminus-todo.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  (str "<img src='http://www.lfed-mw.com/animations/under_construction_animated.gif'>"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/docs" [] (response/ok (-> "docs/docs.md" io/resource slurp))))

