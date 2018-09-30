(ns app.ui.components.complex.inputs
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [app.ui.logic.inputs :as l-i]
            #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])))

(defsc Dropdown
  [this {:keys [field-def]}]
  (dom/select {:className "form-control"
               :id        (l-i/field-id field-def)
               :value     (:value field-def)}
              (dom/option nil (str "-- " (:label field-def) " --"))
              (map
               (fn [option] (dom/option {:value (first option) :key (first option)} (last option)))
               (:options field-def))))

(def dropdown (prim/factory Dropdown))

(defmulti field-def->input
  (fn [field-def] (keyword (-> field-def :field-kind name) (-> field-def :data-type name))))

(defmethod field-def->input :lookup/char [field-def]
  (dropdown {:field-def field-def}))

(defmethod field-def->input :lookup/integer [field-def]
  (dropdown {:field-def field-def}))


(defmethod field-def->input :default [field-def]
  (dom/input {:type        "text"
              :className   "form-control"
              :id          (l-i/field-id field-def)
              :placeholder (:label field-def)
              :value       (:value field-def)
              ; :size ?
              :readOnly    (:read-only field-def)}))

(defsc Input
  [this {:keys [field-def]}]
  (field-def->input field-def))

(def input (prim/factory Input))
