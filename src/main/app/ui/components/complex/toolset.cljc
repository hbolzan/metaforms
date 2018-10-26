(ns app.ui.components.complex.toolset
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [app.ui.components.complex.types :as types]
            #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])))

(def button-types {:insert  {:icon "plus-circle"  :enabled-states [true  true  false] :event :onAppend}
                   :delete  {:icon "trash-alt"    :enabled-states [false true  false] :event :onDelete}
                   :edit    {:icon "edit"         :enabled-states [false true  false] :event :onEdit}
                   :confirm {:icon "check-circle" :enabled-states [false false true]  :event :onConfir}
                   :discard {:icon "ban"          :enabled-states [false false true]  :event :onDiscard}
                   :search  {:icon "search"       :enabled-states [true  true  true]  :event :onSearch}
                   :refresh {:icon "redo"         :enabled-states [false true  true]  :event :onRefresh}})

(defn disabled? [form-state enabled-states]
  (not (boolean (get enabled-states (.indexOf types/form-states (:state form-state))))))

(defn on-click-event
  [this events button-type]
  (if-let [event ((-> button-types button-type :event) events)]
    (do
      {:onClick #(event this)})))

(defn button-props [this state events enabled-states button-type]
  (merge
   {:type      "button"
    :className "btn btn-primary btn-lg"}
   (on-click-event this events button-type)
   (cond (disabled? state enabled-states)
         {:disabled :disabled})))

(defsc FormButton
  [this {:keys [form/state] :as props} {:keys [events button-type]}]
  (let [icon-class (-> button-types button-type :icon)
        enabled-states (-> button-types button-type :enabled-states)]
    (dom/button (button-props this state events enabled-states button-type)
                (dom/i {:className (str "fas fa-" icon-class)}))))

(def form-button (prim/factory
                  FormButton
                  {:keyfn (fn [props] (-> props :fulcro.client.primitives/computed :button-type))}))

(defsc FormToolset
  [this props events]
  (dom/div
   {:className "btn-group" :role "group"}
   (map (fn [button-type] (form-button (prim/computed props
                                                     (merge events {:button-type button-type}))))
        (keys button-types))))

(def toolset (prim/factory FormToolset))
