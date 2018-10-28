(ns app.ui.components.complex.form
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [app.data-samples.forms :as samples]
            [app.ui.logic.complex-forms :as l-cf]
            [app.ui.logic.inputs :as l-i]
            [app.ui.components.widgets :as widgets]
            [app.ui.components.complex.inputs :as w-i]
            [app.ui.components.complex.toolset :as toolset]
            [app.ui.components.complex.types :as types]
            [fulcro.client.dom :as dom]))

(defsc Field
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

(def field (prim/factory Field {:keyfn :field/name}))

(defn width->col-md-class [width]
  (str "col-md-" width))

(defsc Row
  [this {:keys [row-def]}]
  (let [row-fields (map (fn [field-def bootstrap-width]
                          (field (prim/computed {:react-key (str "field-" (:name field-def))
                                                 :field-def field-def}
                                                {:additional-group-class bootstrap-width})))
                        (:defs row-def)
                        (map width->col-md-class (:bootstrap-widths row-def)))]
    (if (= (count (:defs row-def)) 1)
      row-fields
      (dom/div {:className "form-row"}
               row-fields))))

(def row (prim/factory Row))

(defsc FormFields
  [this rows-defs]
  (map-indexed (fn [index row-def] (row (prim/computed {:react-key (str "form-row-" index)}
                                                      row-def)))
               rows-defs))

(def form-fields (prim/factory FormFields))

(defsc Form
  [this {:form/keys [id title state fields] :as props}]
  {:ident         [:form/by-id :form/id]
   :query         [:form/id
                   :form/title
                   :form/state
                   {:form/fields (prim/get-query Field)}]
   :initial-state (fn [form-definition] {:form/id    (:id form-definition)
                                        :form/title (:title form-definition)
                                        :form/state :new
                                        :form/fields (mapv #(prim/get-initial-state Field %) (:fields-defs form-definition))})}
  (widgets/base
   {:title   title
    :toolbar (toolset/toolset (prim/computed props {:events l-cf/form-events}))}
   (dom/div nil (mapv #(field %) fields))))

(def form (prim/factory Form))
