(ns metaforms.ui.components.complex.toolset
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [metaforms.ui.components.complex.types :as types]
            #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])))

(def button-types {:insert  {:icon           "plus-circle"
                             :enabled-states [:empty :view]
                             :form-event     :append}
                   :delete  {:icon           "trash-alt"
                             :enabled-states [:view]
                             :form-event     :delete}
                   :edit    {:icon           "edit"
                             :enabled-states [:view]
                             :form-event     :edit}
                   :confirm {:icon           "check-circle"
                             :enabled-states [:edit]
                             :form-event     :confirm}
                   :discard {:icon           "ban"
                             :enabled-states [:edit]
                             :form-event     :discard}
                   :search  {:icon           "search"
                             :enabled-states [:empty :view]
                             :form-event     :search}
                   :refresh {:icon           "redo"
                             :enabled-states [:view]
                             :form-event     :refresh}})

(defn disabled? [form-state enabled-states]
  (-> (.indexOf enabled-states form-state) (< 0)))

(defn on-click-event
  [this events button-type]
  (if-let [event ((-> button-types button-type :form-event) events)]
    {:onClick #(event this)}))

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
