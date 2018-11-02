(ns metaforms.ui.components.data
  (:require [fulcro.client.mutations :refer [defmutation]]
            [fulcro.client.primitives :as prim :refer [defsc]]
            [metaforms.api.mutations :as api]))

(defsc DataField
  [this
   {:data-field/keys [id dataset-name name value]}]
  {:ident         (fn [] [:data-field/id (keyword dataset-name name)])
   :initial-state (fn [{:keys [dataset-name name value]}] {:data-field/id           (keyword dataset-name name)
                                                          :data-field/dataset-name dataset-name
                                                          :data-field/name         name
                                                          :data-field/value        value})
   :query         [:data-field/id
                   :data-field/dataset-name
                   :data-field/name
                   :data-field/value]})

(defn new-field [data-row dataset-name {:keys [name]}]
  (prim/get-initial-state
   DataField {:dataset-name dataset-name
              :name         name
              :value        (:value data-row)}))

(defn new-row [{:keys [dataset-name id-key fields-defs data-row]}]
  {:record/id           (keyword dataset-name (id-key fields-defs))
   :record/dataset-name dataset-name
   :record/id-key       id-key
   :record/state        :browse
   :record/fields       (mapv (partial new-field dataset-name data-row) fields-defs)})

(defsc DataRecord
  [this
   {:record/keys [id dataset-name id-key state fields]}]
  {:ident         (fn [] [:record/by-id (keyword dataset-name id)])
   :initial-state new-row
   :query         [:record/id
                   :record/id-key
                   :record/state
                   {:record/fields [(prim/get-query DataField)]}]})

(defsc DataSet
  [this {:dataset/keys [name id-key records]}]
  {:ident         [:dataset/by-name :dataset/name]
   :initial-state (fn [{:keys [name id-key fields-defs data-rows]}]
                    {:dataset/name    name
                     :dataset/id-key  id-key
                     :dataset/records (mapv #(prim/get-initial-state DataRecord {:dataset-name name
                                                                                 :id-key       id-key
                                                                                 :fields-defs  fields-defs
                                                                                 :data-row     %})
                                            data-rows)})
   :query         [:dataset/name
                   :dataset/id-key
                   {:dataset/records [(prim/get-query DataRecord)]}]})
