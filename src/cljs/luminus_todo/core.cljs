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
     :search-input ""}))

(re-frame/register-handler
  :process-lists-response
  (fn [app-state [_ response]]
    (println "res: " response)
    (assoc-in app-state [:lists] (map (fn [lists-container]
                                          { :id
                                            (:id lists-container)
                                            :todos
                                            (:todos lists-container)
                                            :title
                                            (:title lists-container)})
                                  response))))

;;(map (fn [lists-container] (hash-map (keyword (:id lists-container)) (:todos lists-container))) response)

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

(re-frame/register-sub
  :lists
  (fn [db]
    (reaction (:lists @db))))

;(re-frame/register-handler
  ;:delete-todo
  ;(fn [app-state _]))

(defn btn-changer
  [doneness]
  (if doneness
    {:class "btn btn-sm btn-primary" :value "Undo" :type "submit"}
    {:class "btn btn-sm btn-success" :value "Complete" :type "submit"}))

(defn todo-cluster
  [todo]
  (fn [todo]
    [:li.list-group-item
      [:div.well.well-sm (if (:done todo)
                            [:del (:description todo)]
                            (:description todo))]
      [:div.input-group
          [:div.input-group-btn
            [:form {:action (str "api/update-todo/" (:id todo))
                    :method "POST"}
              [:input (btn-changer (:done todo))]
              [:input {:type "hidden"
                       :name "done"
                       :value (str (:done todo))}]
              [:input {:type "hidden"
                       :name "description"
                       :value (str (:description todo))}]
              [:input {:type "hidden"
                       :name "list-id"
                       :value (str (:list todo))}]
              [:input {:type "hidden"
                       :name "name"
                       :value (str (:name todo))}]]
            [:form {:action (str "/delete-todo/" (:id todo))
                    :method "POST"}
              [:input.btn.btn-danger.btn-sm {:type "submit"
                                             :value "Delete this Todo"}]]]]]))

(defn display-todo-list
  []
  (fn []
    [:div.col-md-4
      [:div.panel.panel-default
        [:div.panel-heading (str (:title list-info))
          [:a.btn.btn-danger.btn-sm.pull-right {:href (str "api/delete-list/" (:id list-info)) :style (str "visibility:" (if (empty? (:todos list-info)) "visible" "hidden"))}
            [:span.glyphicon.glyphicon-minus]]
          [:div.panel-body
            [:div.row
              [:ul.list-group
                (map todo-cluster (:todos list-info))]
              [:form {:action "api/add-todo" :method "POST"}
                [:div.input-group]
                [:input {:type "hidden"
                         :name "list"
                         :value (:id list-info)}]
                [:input.form-control {:type "Text"
                                      :name "description"
                                      :placeholder "Todo: "}]
                [:span.input-group-btn]
                [:input.btn.btn-success {:type "submit"
                                         :value "Add todo to list"}]]]]]]]))

(defn list-add-form
  []
  (fn []
    [:div.col-md-4
      [:form {:action (str "/api/add-list")
              :method "POST"}
        [:div.input-group
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
            ;; (println @lists)
            [:ul (for [todo-list @lists]
                    ^{:key (:id todo-list)}
                    [:li (str todo-list)])]
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
