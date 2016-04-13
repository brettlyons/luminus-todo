(ns luminus-todo.core
  (:require [reagent.core :as r :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [luminus-todo.ajax :refer [load-interceptors!]]
            [re-frame.core :as re-frame]
            [re-com.core :as re-com]
            [ajax.core :refer [GET POST]])
  (:require-macros [reagent.ratom :refer [reaction]])
  (:import goog.History))

;; -- NAVIGATION

(defn nav-link [uri title page collapsed?]
  [:li.nav-item
   {:class (when (= page (session/get :page)) "active")}
   [:a.nav-link
    {:href uri
     :on-click #(reset! collapsed? true)} title]])

(defn navbar []
  (let [collapsed? (r/atom true)]
    (fn []
      [:nav.navbar.navbar-light.bg-faded
       [:button.navbar-toggler.hidden-sm-up
        {:on-click #(swap! collapsed? not)} "☰"]
       [:div.collapse.navbar-toggleable-xs
        (when-not @collapsed? {:class "in"})
        [:a.navbar-brand {:href "#/"} "luminus-todo"]
        [:ul.nav.navbar-nav
         [nav-link "#/" "Home" :home collapsed?]
         [nav-link "#/about" "About" :about collapsed?]]]])))

;; -- HANDLERS / SUBSCRIPTIONS

(re-frame/register-handler
  :initialize-db
  (fn [db v]
    {:lists []
     :todos {}
     :search-input ""}))

(re-frame/register-handler
  :process-lists-response
  (fn [app-state [_ response]]
    (re-frame/dispatch [:load-todos (map :id response)])
    (assoc-in app-state [:lists] response)))

(re-frame/register-handler
  :process-todos-response
  (fn [app-state [_ response list-id]]
    (println "TODOS HANDLER RESPONSE" response "list id" list-id)
    (assoc-in app-state [:todos] (concat (:todos app-state) response))))
;; this puts it all into one big list -- which I do not like.
;; I would much rather have {listid (todo1, todo2) listid2 (todo1, todo2)}

;(map :id response)
;(map #(zipmap (keys %)
              ;(vals %)))

(re-frame/register-handler
  :process-lists-bad-response
  (fn [app-state [_ response]]
    (println "ERROR: " response)
    app-state))

(re-frame/register-handler
  :load-lists
  (fn [app-state _]
    (GET "api/get-lists"
              {:handler #(re-frame/dispatch [:process-lists-response %1])
               :error-handler #(re-frame/dispatch [:process-lists-bad-response %1])})
    app-state))

(re-frame/register-handler
  :load-todos
  (fn [app-state [_ remaining-list-ids]]
    (let [list-id (first remaining-list-ids)]
      (println "rem ids" remaining-list-ids list-id)
      (println app-state)
      (GET (str "api/get-todos/" list-id)
          {:handler #(re-frame/dispatch [:process-todos-response %1 list-id])
           :error-handler #(re-frame/dispatch [:process-lists-bad-response %1])})
      (if (not-empty (next remaining-list-ids))
        (recur app-state [_ (next remaining-list-ids)])
        app-state))))



;(re-frame/register-handler
  ;:delete-todo
  ;(fn [app-state [_ todo-id]]
    ;(POST (str "api/delete-todo/" todo-id)
          ;{:handler #()
           ;:error-handler #(println "ERROR DELETING: " %1)})
    ;(app-state))) ;; we need to remove stuff via last line here

(re-frame/register-sub
  :lists
  (fn [db]
    (reaction (:lists @db))))

(re-frame/register-sub
  :todos
  (fn [db]
    (reaction (:todos @db))))

(defn btn-changer
  [doneness]
  (fn [doneness]
    (if doneness
      [:button.btn.btn-primary.btn-block "Undo"]
      [:button.btn.btn-success.btn-block "Complete"])))

(defn todo-cluster
  [todo]
  (fn [todo]
    [:div.row
      [:div.list-group-item (if (:done todo)
                              [:del (:description todo)]
                              (:description todo))
        [:div.input-group
          [btn-changer (:done todo)]
          [:button.btn.btn-danger.btn-block "Delete"]]]]))

(defn display-todo-list
  [list-info]
  (let [all-lists (re-frame/subscribe [:todos])]
  ; (let [lists (filter #(= (:id list-info) (:list %)) (re-frame/subscribe [:todos]))]
    (fn [list-info]
      (println (filter #(= (:id list-info) (:list %)) @all-lists))
      (let [todo-list (filter #(= (:id list-info) (:list %)) @all-lists)]
        ;; need to filter based on list-id, so todos from list2 goes to list2 etc.
        [:div.col-xs-6
          [:div.card
            [:div.card-header.card-title (str (:title list-info))
              ;[:a.btn.btn-danger.btn-sm.pull-right {:href (str "api/delete-list/" (:id list-info)) :style (str "visibility:" (if (empty? (:todos list-info)) "visible" "hidden"))}
                [:span.glyphicon.glyphicon-minus]
              [:div.card-block
                [:ul.list-group
                  (for [todo todo-list]
                      ^{:key (:id todo)}
                      [todo-cluster todo])]]
              [:div.card-footer
                [:input.form-control {:type "Text"
                                      :name "description"}]
                [:div.row {:style {:margin-bottom "20px"}}]
                [:button.btn.btn-success.btn-block {:on-click (fn [e] (.log js/console e))}; (-> e .-parent .-value)))}
                  "Add todo to list."]]]]]))))


(defn list-add-form
  []
  (fn []
    [:div.col-xs-6
      [:form {:action (str "/api/add-list")
              :method "POST"}
        [:div.input-group.input-group-sm
          [:input.form-control {:type "Text"
                                :name "title"
                                :placeholder "New List Title"}]
          [:span.input-group-btn
            [:input.btn.btn-success {:type "submit"
                                     :value "Add new list"}]]]]]))

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "This project is a todo list implementation.  For DemocracyWorks!"]]])

(defn home-page []
  (let [lists (re-frame/subscribe [:lists])]
    (fn []
      [:div.container
        [:div.jumbotron
          [:h1 "Welcome to luminus-todo lists"]]
        [:div.row
          [:div.col-md-12
            (for [todo-list @lists]
              ^{:key (:id todo-list)}
              [display-todo-list todo-list])
            [list-add-form]]]])))

(def pages
  {:home #'home-page
   :about #'about-page})

(defn page []
  [(pages (session/get :page))])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :page :home))

(secretary/defroute "/about" []
  (session/put! :page :about))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

;; -------------------------
;; Initialize app
;(defn fetch-docs! []
  ;(GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (load-interceptors!)
  ;(fetch-docs!)
  (hook-browser-navigation!)
  (re-frame/dispatch [:initialize-db])
  (re-frame/dispatch [:load-lists])
  (mount-components))
