(ns app.ui.components.complex.form
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [app.data-samples.forms :as samples]
            [app.ui.logic.complex-forms :as l-cf]
            [app.ui.logic.inputs :as l-i]
            [app.ui.components.widgets :as widgets]
            [app.ui.components.data :as data]
            [app.ui.components.complex.inputs :as w-i]
            [app.ui.components.complex.toolset :as toolset]
            [app.ui.components.complex.types :as types]
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

(defsc Form
  [this {:form/keys [id title state fields rows-defs] :as props}]
  {:ident         [:form/by-id :form/id]
   :query         [:form/id
                   :form/title
                   :form/state
                   {:form/fields (prim/get-query FormField)}
                   :form/rows-defs]
   :initial-state (fn
                    [{fields-defs :fields-defs :as form-definition}]
                    {:form/id        (:id form-definition)
                     :form/title     (:title form-definition)
                     :form/state     :empty
                     :form/fields    (mapv #(prim/get-initial-state FormField %) fields-defs)
                     :form/rows-defs (l-cf/distribute-fields fields-defs l-cf/bootstrap-md-width)})}
  (widgets/base
   {:title   title
    :toolbar (toolset/toolset (prim/computed props {:events l-cf/form-events}))}
   (dom/div nil (map-indexed (fn [index row-def] (form-row index row-def fields)) rows-defs))))

(def form (prim/factory Form))
