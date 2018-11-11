(ns metaforms.ui.components.complex.inputs
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [metaforms.ui.logic.inputs :as l-i]
            [metaforms.ui.logic.complex-forms :as l-cf]
            [fulcro.client.dom :as dom]))

(defn dropdown [{:keys [field-id label value options on-change]}]
  (dom/select {:className "form-control"
               :id        field-id
               :value     value
               :onChange  on-change}
              (dom/option {:value ""} (str "-- " label " --"))
              (map
               (fn [option] (dom/option {:value (first option) :key (first option)} (last option)))
               options)))

(defmulti field-def->input
  (fn [field-def this] (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def this]
  (dropdown field-def))

(defmethod field-def->input :lookup/integer [field-def this]
  (dropdown field-def))

(defn field-def->input-params [{:keys [name field-id label value on-change read-only]}]
  {:type        "text"
   :className   "form-control"
   :name        name
   :id          field-id
   :placeholder label
   :value       value
   :onChange    (fn [e] (on-change e))
   :readOnly    read-only})

(defmethod field-def->input :default [field-def _]
  (dom/input (field-def->input-params field-def))
  #_(dom/input {:type        "text"
              :className   "form-control"
              :name        name
              :id          field-id
              :placeholder label
              :value       value
                                        ; :size ?
              :readOnly    read-only
              ;; :onChange
              #_(fn [e] (l-cf/on-change-field
                                 this
                                 (:name field-def)
                                 (:event e)))}))

(defsc Input
  [this {:keys [input/id field-def]}]
  {:ident [:input/by-id :input/id]
   :initial-state (fn [id] {:input/id id})
   :query [:input/id
           :field-def]}
  (field-def->input field-def this))

(def input (prim/factory Input {:keyfn #(l-i/field-id (:field-def %))}))
