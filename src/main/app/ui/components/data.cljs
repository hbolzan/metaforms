(ns app.ui.components.data
  (:require [fulcro.client.mutations :refer [defmutation]]
            [fulcro.client.primitives :as prim :refer [defsc]]
            [app.api.mutations :as api]))

(defsc DataField
  [this
   {:data-field/keys [form name value]}]
  {:ident (fn [this props] [:data-field/id (keyword (:form props) (:name props))])
   :initial-state []})

(defsc DataRecord
  [this
   {:record/keys [id id-key values]}]
  {:ident         [:record/by-id :record/id]
   :initial-state (fn [{:keys [id-key values]}]
                    {:id     (id-key values)
                     :id-key id-key
                     :values values})
   :query         [:record/id
                   :record/id-key
                   :record/values]})
