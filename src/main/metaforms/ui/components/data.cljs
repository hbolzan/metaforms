(ns metaforms.ui.components.data
  (:require [fulcro.client.mutations :refer [defmutation]]
            [fulcro.client.primitives :as prim :refer [defsc]]
            [metaforms.api.mutations :as api]))

(defsc DataField
  [this
   {:data-field/keys [id data-record-id name value]}]
  {:ident         [:data-field/by-id :data-field/id]
   :initial-state (fn [{:keys [data-record-id name value]}] {:data-field/id             (str data-record-id "__" name)
                                                             :data-field/data-record-id data-record-id
                                                             :data-field/name           name
                                                             :data-field/value          value})
   :query         [:data-field/id
                   :data-field/data-record-id
                   :data-field/name
                   :data-field/value]})

(defn new-field [data-row data-record-id {:keys [name]}]
  (prim/get-initial-state
   DataField {:data-record-id data-record-id
              :name           name
              :value          (get data-row name)}))

(defsc DataRecord
  [this
   {:data-record/keys [id dataset-name id-key state fields]}]
  {:ident         [:data-record/by-id :data-record/id]
   :initial-state (fn [{:keys [dataset-name id-key fields-defs data-row]}]
                    (let [record-id (str dataset-name "__" (get data-row id-key))]
                      {:data-record/id           record-id
                       :data-record/dataset-name dataset-name
                       :data-record/id-key       id-key
                       :data-record/state        :browse
                       :data-record/fields       (mapv
                                                  (partial new-field data-row record-id)
                                                  fields-defs)}))
   :query         [:data-record/id
                   :data-record/dataset-name
                   :data-record/id-key
                   :data-record/state
                   {:data-record/fields (prim/get-query DataField)}]})

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
                   {:dataset/records (prim/get-query DataRecord)}]})
