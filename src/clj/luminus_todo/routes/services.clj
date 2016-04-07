(ns luminus-todo.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [luminus-todo.db.core :as db]
            [schema.core :as s]))

(s/defschema Thingie {:id Long
                      :hot Boolean
                      :tag (s/enum :kikka :kukka)
                      :chief [{:name String
                               :type #{{:id String}}}]})

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}
  (context "/api" []
    :tags ["todos"]

    ;(GET "/plus" []
      ;:return       Long
      ;:query-params [x :- Long, {y :- Long 1}]
      ;:summary      "x+y with query-parameters. y defaults to 1."
      ;(ok (+ x y)))

    (POST "/add-todo" []
      ; :form-params [description :- String, list :- Long]
      :form-params [description :- String]
      :summary     "Post todos address"
      (println description)
      (db/create-todo! {:description description})
      (ok))
    (POST "/update-todo/:id" []
      :path-params [id :- Long]
      :form-params [done :- Boolean]
      :summary "Update the doneness status of a todo"
      (db/update-done! {:id id :done done})
      (ok))
    (POST "/delete-todo/:id" []
      :path-params [id :- Long]
      :summary "Delete the todo with this id"
      (db/delete-todo! {:id id})
      (ok))
    (GET "/get-lists-joined-todos" []
      :summary "Returns todos joined with lists"
      (ok (db/get-lists-joined-todos)))
    (GET "/get-todos/:list-id" []
      :summary "Returns the todos for a given list"
      :path-params [list-id :- Long]
      (ok (db/get-todos {:id list-id})))
    ;(GET "/times/:x/:y" []
      ;:return      Long
      ;:path-params [x :- Long, y :- Long]
      ;:summary     "x*y with path-parameters"
      ;(ok (* x y)))

    ;(POST "/divide" []
      ;:return      Double
      ;:form-params [x :- Long, y :- Long]
      ;:summary     "x/y with form-parameters"
      ;(ok (/ x y)))

    ;(GET "/power" []
      ;:return      Long
      ;:header-params [x :- Long, y :- Long]
      ;:summary     "x^y with header-parameters"
      ;(ok (long (Math/pow x y))))

    ;(PUT "/echo" []
      ;:return   [{:hot Boolean}]
      ;:body     [body [{:hot Boolean}]]
      ;:summary  "echoes a vector of anonymous hotties"
      ;(ok body))

    ;(POST "/echo" []
      ;:return   (s/maybe Thingie)
      ;:body     [thingie (s/maybe Thingie)]
      ;:summary  "echoes a Thingie from json-body"
      ;(ok thingie)))

  (context "/context" []
    :tags ["context"]
    :summary "summary inherited from context"
    (context "/:kikka" []
      :path-params [kikka :- s/Str]
      :query-params [kukka :- s/Str]
      (GET "/:kakka" []
        :path-params [kakka :- s/Str]
        (ok {:kikka kikka
             :kukka kukka
             :kakka kakka}))))
))
