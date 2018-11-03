(ns metaforms.ui.components.complex.form
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client.mutations :refer [defmutation]]
            [metaforms.ui.logic.complex-forms :as l-cf]
            [metaforms.ui.logic.inputs :as l-i]
            [metaforms.ui.components.widgets :as widgets]
            [metaforms.ui.components.data :as data]
            [metaforms.ui.components.complex.inputs :as w-i]
            [metaforms.ui.components.complex.toolset :as toolset]
            [metaforms.ui.components.complex.types :as types]
            [fulcro.client.dom :as dom]))

(defsc FormField
  [this
   {:field/keys [id name label kind read-only data-type width options value]}
   {:keys [additional-group-class]}]
  {:ident         [:field/by-name :field/name]
   :initial-state (fn
                    [{:keys [name label field-kind read-only data-type width options] :as field-def}]
                    {:field/id        (l-i/field-id field-def)
                     :field/name      name
                     :field/label     label
                     :field/kind      field-kind
                     :field/read-only read-only
                     :field/data-type data-type
                     :field/width     width
                     :field/options   options
                     :field/value     ""})
   :query         [:field/id
                   :field/name
                   :field/label
                   :field/kind
                   :field/read-only
                   :field/data-type
                   :field/width
                   :field/options
                   :field/value]}
  (dom/div {:className (str "form-group" (some->> additional-group-class (str " ")))}
           (dom/label {:htmlFor id} label)
           (w-i/field-def->input {:field-id   id
                                  :field-kind kind
                                  :data-type  data-type
                                  :name       name
                                  :label      label
                                  :value      value
                                  :options    options
                                  :read-only  read-only})))

(def form-field (prim/factory FormField {:keyfn :field/name}))

(defn form-row [row-index row-def fields]
  (dom/div
   {:className "form-row" :key (str "row-" row-index)}
   (map (fn
          [field bootstrap-width]
          (form-field (prim/computed
                       field {:additional-group-class (l-cf/width->col-md-class bootstrap-width)})))
        (l-cf/row-fields row-def fields)
        (:bootstrap-widths row-def))))

(defmutation set-form-state [{:keys [form-id new-state]}]
  (action [{:keys [state]}]
          (let [form-ident [:form/by-id form-id]]
            (swap! state assoc-in (conj form-ident :form/state) new-state))))

(defn form-set-state [new-state form-id component]
  (prim/transact! component `[(set-form-state {:form-id ~form-id :new-state ~new-state})]))

(def form-append (partial form-set-state :edit))
(def form-confirm (partial form-set-state :view))
(def form-discard (partial form-set-state :empty))

(defn form-events [form-id]
  {:events {:append  #(form-append form-id %)
            :confirm #(form-confirm form-id %)
            :discard #(form-discard form-id %)}})

(defsc Form
  [this {:form/keys [id title state fields rows-defs dataset] :as props}]
  {:ident         [:form/by-id :form/id]
   :query         [:form/id
                   :form/title
                   :form/state
                   :form/rows-defs
                   {:form/fields (prim/get-query FormField)}
                   {:form/dataset (prim/get-query data/DataSet)}]
   :initial-state (fn
                    [{{fields-defs :fields-defs} :form-definition :as form-definition
                      dataset         :dataset}]
                    {:form/id        (:id form-definition)
                     :form/title     (:title form-definition)
                     :form/state     :empty
                     :form/fields    (mapv #(prim/get-initial-state FormField %) fields-defs)
                     :form/rows-defs (l-cf/distribute-fields fields-defs l-cf/bootstrap-md-width)
                     :form/dataset   (prim/get-initial-state data/DataSet (assoc dataset :fields-defs fields-defs))})}
  (widgets/base
   {:title   title
    :toolbar (toolset/toolset (prim/computed props (form-events id)))}
   (dom/div nil (map-indexed (fn [index row-def] (form-row index row-def fields)) rows-defs))))

(def form (prim/factory Form))
