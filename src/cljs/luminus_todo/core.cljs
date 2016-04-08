(ns luminus-todo.core
  (:require [reagent.core :as r]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [luminus-todo.ajax :refer [load-interceptors!]]
            [re-frame.core :as re-frame]
            [re-com.core :as re-com]
            [ajax.core :refer [GET POST]])
  (:import goog.History))



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


(defn btn-changer
  [doneness]
  (if doneness
    {:class "btn btn-sm btn-primary" :value "Undo" :type "submit"}
    {:class "btn btn-sm btn-success" :value "Complete" :type "submit"}))

(defn todo-cluster
  [todo]
  (fn []
    [:li.list-group-item
      [:div.well.well-sm (if (:done todo)
                            [:del (:description todo)]
                            (:description todo))]
      [:div.input-group
          [:div.input-group-btn
            [:form {:action (str "/update-done/" (:id todo)) :method "POST"}
              (anti-forgery-field)
              [:input (btn-changer (:done todo))]
              [:input {:type "hidden" :name "done" :value (str (:done todo))}]
              [:input {:type "hidden" :name "description" :value (str (:description todo))}]
              [:input {:type "hidden" :name "list" :value (str (:list todo))}]
              [:input {:type "hidden" :name "name" :value (str (:name todo))}]]
            [:form {:action (str "/delete-todo/" (:id todo)) :method "POST"}
              (anti-forgery-field)
              [:input.btn.btn-danger.btn-sm {:type "submit" :value "Delete this Todo"}]]]]]))

(defn display-todo-list
  [list-info]
  (fn []
    [:div.col-md-4
      [:div.panel.panel-default
        [:div.panel-heading (str (:title list-info))
          [:a.btn.btn-danger.btn-sm.pull-right {:href (str "/delete-list/" (:id list-info)) :style (str "visibility:" (if (empty? (:todos list-info))
                                                                                                                        "visible"
                                                                                                                        "hidden"))}
            [:span.glyphicon.glyphicon-minus]]
          [:div.panel-body
            [:div.row]
            [:ul.list-group
              (map todo-cluster (:todos list-info))]
            [:form {:action "/add-todo" :method "POST"}
              (anti-forgery-field)
              [:div.input-group]
              [:input {:type "hidden" :name "list" :value (:id list-info)}]
              [:input.form-control {:type "Text" :name "description" :placeholder "Todo: "}]
              [:span.input-group-btn]
              [:input.btn.btn-success {:type "submit" :value "Add todo to list"}]]]]]]))

(defn list-add-form
  []
  (fn []
    [:div.col-md-4
      [:form {:action (str "/add-list") :method "POST"}
        [:div.input-group
          [:input.form-control {:type "Text" :name "title" :placeholder "New List Title"}]
          [:span.input-group-btn
            [:input.btn.btn-success {:type "submit" :value "Add new list"}]]]]]))

(defn main-page
  [data-model]
  (fn []
    [:div
      (layout-common "The Todo Lists" (map display-todo-list data-model))
      (list-add-form)]))


(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     "This project is a todo list implementation.  For DemocracyWorks!"]]])

(defn home-page []
  [:div.container
   [:div.jumbotron
    [:h1 "Welcome to luminus-todo lists"]
    [:p "Time to start building your site!"]]
   [:div.row
    [:div.col-md-12
     [:h2 "some text"]]]
   (when-let [docs (session/get :docs)]
     [:div.row
      [:div.col-md-12]])])

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
(defn fetch-docs! []
  (GET (str js/context "/docs") {:handler #(session/put! :docs %)}))

(defn mount-components []
  (r/render [#'navbar] (.getElementById js/document "navbar"))
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
