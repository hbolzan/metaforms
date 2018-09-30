(ns app.ui.components.complex.toolset
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [app.ui.components.complex.types :as types]
            #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])))

(def button-types {:insert  {:icon "plus-circle"  :enabled-states [true  true  false]}
                   :delete  {:icon "trash-alt"    :enabled-states [false true  false]}
                   :edit    {:icon "edit"         :enabled-states [false true  false]}
                   :confirm {:icon "check-circle" :enabled-states [false false true]}
                   :discard {:icon "ban"          :enabled-states [false false true]}
                   :search  {:icon "search"       :enabled-states [true  true  true]}
                   :refresh {:icon "redo"         :enabled-states [false true  true]}})

(defn disabled? [form-state enabled-states]
  (not (boolean (get enabled-states (.indexOf types/form-states (:state form-state))))))

(defn button-props [form-state enabled-states]
  (merge
   {:type      "button"
    :className "btn btn-primary btn-lg"}
   (cond (disabled? form-state enabled-states)
         {:disabled :disabled})))

(defsc FormButton
  [this {:keys [form-state button-type]}]
  (let [icon-class (-> button-types button-type :icon)
        enabled-states (-> button-types button-type :enabled-states)]
    (dom/button (button-props form-state enabled-states)
                (dom/i {:className (str "fas fa-" icon-class)}))))

(def form-button (prim/factory FormButton {:keyfn :button-type}))

(defsc FormToolset
  [this {:keys [form-state]} events]
  (dom/div
   {:className "btn-group" :role "group"}
   (map (fn [button-type] (form-button {:form-state form-state
                                       :button-type button-type}))
        (keys button-types))))

(def toolset (prim/factory FormToolset))
