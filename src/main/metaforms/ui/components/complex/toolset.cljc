(ns metaforms.ui.components.complex.toolset
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [metaforms.ui.components.complex.types :as types]
            #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])))

(def action-buttons {:insert {:icon           "plus-circle"
                              :enabled-states [:empty :view]
                              :form-event     :append}
                     :delete   {:icon           "trash-alt"
                                :enabled-states [:view]
                                :form-event     :delete}
                     :edit     {:icon           "edit"
                                :enabled-states [:view]
                                :form-event     :edit}
                     :confirm  {:icon           "check-circle"
                                :enabled-states [:edit]
                                :form-event     :confirm}
                     :discard  {:icon           "ban"
                                :enabled-states [:edit]
                                :form-event     :discard}
                     :search   {:icon           "search"
                                :enabled-states [:empty :view]
                                :form-event     :search}
                     :refresh  {:icon           "redo"
                                :enabled-states [:view]
                                :form-event     :refresh}})

(def nav-buttons {:first {:icon           "fast-backward"
                          :enabled-states [:empty :view :edit]
                          :form-event     :nav-first}
                  :prior {:icon           "step-backward"
                          :enabled-states [:empty :view :edit]
                          :form-event     :nav-prior}
                  :next  {:icon           "step-forward"
                          :enabled-states [:empty :view :edit]
                          :form-event     :nav-next}
                  :last  {:icon           "fast-forward"
                          :enabled-states [:empty :view :edit]
                          :form-event     :nav-last}}
  )

(defn disabled? [form-state enabled-states]
  (-> (.indexOf enabled-states form-state) (< 0)))

(defn on-click-event
  [this events button-type button-types]
  (if-let [event ((-> button-types button-type :form-event) events)]
    {:onClick #(event this)}))

(defn button-props [this state events enabled-states button-type button-types]
  (merge
   {:type      "button"
    :className "btn btn-primary btn-lg"}
   (on-click-event this events button-type button-types)
   (cond (disabled? state enabled-states)
         {:disabled :disabled})))

(defsc FormButton
  [this {:keys [form/state] :as props} {:keys [events button-type button-types]}]
  (let [icon-class     (-> button-types button-type :icon)
        enabled-states (-> button-types button-type :enabled-states)]
    (dom/button (button-props this state events enabled-states button-type button-types)
                (dom/i {:className (str "fas fa-" icon-class)}))))

(def form-button (prim/factory
                  FormButton
                  {:keyfn (fn [props] (-> props :fulcro.client.primitives/computed :button-type))}))

(defn btn-group
  [props events buttons]
  (dom/div
   {:className "btn-group mr-2" :role "group"}
   (map (fn [button-type] (form-button (prim/computed props
                                                     {:events       events
                                                      :button-type  button-type
                                                      :button-types buttons})))
        (keys buttons))))

(defn toolset
  [props events]
  (dom/div
   {:className "btn-toolbar" :role "toolbar"}
   (btn-group props events action-buttons)
   (btn-group props events nav-buttons)))

;; (def toolset (prim/factory FormToolset))
