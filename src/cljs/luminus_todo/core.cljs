(ns luminus-todo.core
  (:require [reagent.core :as r :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [luminus-todo.ajax :refer [load-interceptors!]]
            [re-frame.core :as re-frame]
            ; [re-com.core :as re-com]
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

;; I return to the idea that . .
;; the lists vector should contain a list object that looks like this:
;; {:list-id 1
;;  :title "Some list title"
;;  :todos ({:id 1 :description "The todo" :done false})
;;  :new-todo-content ""} {etc...}

;;  ^ this.


(re-frame/register-handler
  :process-lists-response
  (fn [app-state [_ response]]
    (println "Get-lists response: " response)
    ;(re-frame/dispatch [:load-todos (map :id response)])
    (assoc-in app-state [:lists] response)))

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
  :process-todos-response
  (fn [app-state [_ response list-id]]
    (assoc-in app-state [:todos] (concat (:todos app-state) response))))

(re-frame/register-handler
  :load-todos
  (fn [app-state [_ remaining-list-ids]]
    (let [list-id (first remaining-list-ids)]
      (GET (str "api/get-todos/" list-id)
        {:handler #(re-frame/dispatch [:process-todos-response % list-id])
         :error-handler #(re-frame/dispatch [:process-lists-bad-response %])})
      (if (not-empty (next remaining-list-ids))
        (recur app-state [_ (next remaining-list-ids)])
        app-state))))

(re-frame/register-handler
  :process-delete-success
  (fn [app-state [_ todo-id]]
    (assoc-in app-state [:todos] (remove #(= todo-id %) (:todos app-state)))))

(re-frame/register-handler
  :delete-todo
  (fn [app-state [_ todo-id]]
    (POST (str "api/delete-todo/" todo-id)
          {:handler #(re-frame/dispatch [:process-delete-success todo-id])
           :error-handler #(println "ERROR DELETING: " %1)})
    app-state))

(re-frame/register-handler
  :create-todo
  (fn [app-state [_ todo-content list-id]]
    (println "create-todo" @todo-content list-id)
    (re-frame/dispatch [:post-todo @todo-content list-id])
    (assoc-in app-state [:todos] (cons (:todos app-state) {:description @todo-content :done false :list list-id}))))

(re-frame/register-handler
  :post-todo
  (fn [app-state [_ todo-content list-id]]
    ;(POST "api/create-todo"
          ;{:params {:description todo-content
                    ;:list-id list-id}
           ;:handler #(re-frame/dispatch [:process-post-todo])
           ;:error-handler #(println "CREATE-TODO-ERROR: " %)})
    app-state))

(re-frame/register-handler
  :new-todo-change
  (fn [app-state [_ new-content list-id]]
    (println app-state)
    (assoc-in app-state [:new-todo-content list-id] new-content)))

(re-frame/register-sub
  :lists
  (fn [db]
    (reaction (:lists @db))))

(re-frame/register-sub
  :todos
  (fn [db [_ list-id]]
    (reaction (filter #(= list-id (:list %)) (:todos @db)))))

(re-frame/register-sub
  :new-todo-content
  (fn [db [_ list-id]]
    (reaction (second (filter #(= list-id (first %)) (:new-todo-content @db))))))

;; stuck here. -- how I do this?

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
       [:button.btn.btn-danger.btn-block "Delete"]]]])) ;; lets get add before delete
        ;{:on-click (re-frame/dispatch [:delete-todo (:id todo)])}
        ;"Delete"]]]]))

(defn display-todo-list
  [list-info]
  (let [todo-list (re-frame/subscribe [:todos (:id list-info)])
        new-todo-content (re-frame/subscribe [:new-todo-content (:id list-info)])]
    ;(println @todo-list)
    (fn [list-info]
      [:div.col-xs-6
        [:div.card
          [:div.card-header.card-title (str (:title list-info))
            ;[:a.btn.btn-danger.btn-sm.pull-right {:href (str "api/delete-list/" (:id list-info)) :style (str "visibility:" (if (empty? (:todos list-info)) "visible" "hidden"))}
              [:span.glyphicon.glyphicon-minus]
            [:div.card-block
              [:ul.list-group
                (for [todo @todo-list]
                    ^{:key (:id todo)}
                    [todo-cluster todo])]]
            [:div.card-footer
              [:input.form-control {:type "Text"
                                    :name "description"
                                    :value @new-todo-content
                                    :on-change #(re-frame/dispatch [:new-todo-change (-> % .-target .-value) (:id list-info)])}]
              [:div.row {:style {:margin-bottom "20px"}}]
              [:button.btn.btn-success.btn-block
                ;{:on-click #(re-frame/dispatch [:create-todo new-todo-content (:id list-info)])}
                "Add todo to list."]]]]])))


(defn list-add-form
  []
  (fn []
    [:div.col-xs-6
      [:form {:action (str "/api/create-list")
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
