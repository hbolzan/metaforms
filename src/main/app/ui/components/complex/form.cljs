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
   {:keys [field-def]}
   {:keys [additional-group-class]}]
  (dom/div {:className (str "form-group" (some->> additional-group-class (str " ")))}
           (dom/label {:htmlFor (l-i/field-id field-def)} (:label field-def))
           (w-i/input {:field-def field-def :input/id (l-i/field-id field-def)})))

(def field (prim/factory Field))

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
  [this {:form/keys [id definition state] :as props}]
  {:ident         [:form/by-id :form/id]
   :query         [:form/id
                   :form/definition
                   :form/state]
   :initial-state (fn [form-id] (l-cf/form-state-change {:form/id         form-id
                                                        :form/definition samples/form-definition
                                                        :form/state      samples/form-state}))}
  (widgets/base
   {:title   (:title definition)
    :toolbar (toolset/toolset (prim/computed props {:events l-cf/form-events}))}
   (dom/div nil (map-indexed (fn [index row-def] (row {:react-key (str "form-row-" index) :row-def row-def}))
                             (-> props :form/state :rows-defs)))))

(def form (prim/factory Form))
