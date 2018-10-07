(ns app.ui.components.complex.inputs
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [app.ui.logic.inputs :as l-i]
            [app.ui.logic.complex-forms :as l-cf]
            #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])))

(defsc Dropdown
  [this _ {:keys [field-def]}]
  (dom/select {:className "form-control"
               :id        (l-i/field-id field-def)
               :value     (:value field-def)}
              (dom/option nil (str "-- " (:label field-def) " --"))
              (map
               (fn [option] (dom/option {:value (first option) :key (first option)} (last option)))
               (:options field-def))))

(def dropdown (prim/factory Dropdown))

(defmulti field-def->input
  (fn [field-def this] (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def this]
  (dropdown (prim/computed {} {:field-def field-def})))

(defmethod field-def->input :lookup/integer [field-def this]
  (dropdown (prim/computed {} {:field-def field-def})))

(defmethod field-def->input :default [field-def this]
  (dom/input {:type        "text"
              :className   "form-control"
              :name        (:name field-def)
              :id          (l-i/field-id field-def)
              :placeholder (:label field-def)
              :value       (:value field-def)
                                        ; :size ?
              :readOnly    (:read-only field-def)
              :onChange (fn [e] (l-cf/on-change-field
                                 this
                                 (:name field-def)
                                 "fooo"))}))

(defsc Input
  [this {:keys [form]} {:keys [field-def]}]
  (field-def->input field-def this))

(def input (prim/factory Input))
