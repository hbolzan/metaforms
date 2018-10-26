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
   {:form/keys [id definition state] :as form}
   {:keys [field-name field-def additional-group-class]}]
  {:ident         [:field/by-id :ui/id]
   :query         [:form/definition
                   :form/state
                   :ui/id]
   :initial-state (fn [_] {:ui/id (random-uuid)})}
  (dom/div {:className (str "form-group" (some->> additional-group-class (str " ")))}
           (dom/label {:htmlFor id} (:label field-def))
           (w-i/input (prim/computed form
                                     {:field-def field-def}))))

(def field (prim/factory Field {:keyfn :ui/id}))

(defn width->col-md-class [width]
  (str "col-md-" width))


(defsc Row2
  [this {:ui/keys [id]} {:form.row/keys [fields]}]
  {:ident         [:row/by-id :ui/id]
   :initial-state (fn [_] {:ui/id (random-uuid)})
   :query         [:ui/id
                   :form.row/fields [(prim/get-query Field2)]]})

(defsc Row
  [this
   {:form/keys [definition state] :as form
    :ui/keys   [id fields]}
   {:keys [row-def]}]
  {:ident         [:row/by-id :ui/id]
   :initial-state (fn [_] {:ui/id (random-uuid)})
   :query         [:form/definition
                   :form/state
                   :ui/id
                   {:ui/fields [(prim/get-query Field)]}]}
  (if (= (count (:defs row-def)) 1)
    (field {:field-def (-> row-def :defs first)})
    (dom/div {:className "form-row"}
             (map (fn [field-def bootstrap-width]
                    (field (prim/computed form
                                          {:additional-group-class bootstrap-width
                                           :field-def              field-def
                                           :field-name             (:name field-def)})))
                  (:defs row-def)
                  (map width->col-md-class (:bootstrap-widths row-def))))))

(def row (prim/factory Row {:keyfn :ui/id}))

(defsc FormFields
  [this {:keys [definition state] :as form} {:keys [fields-defs form-data]}]
  (let [rows-defs (l-cf/distribute-fields (l-cf/defs<-data fields-defs form-data) l-cf/bootstrap-md-width)]
    (map-indexed (fn [index row-def] (row (prim/computed form
                                                        {:row-index index
                                                         :row-def row-def})))
                 rows-defs)))

(def form-fields (prim/factory FormFields))

(defsc Form
  [this {:form/keys [id definition state] :as props}]
  {:ident         [:form/by-id :form/id]
   :query         [:form/id
                   :form/definition
                   :form/state]
   :initial-state (fn [form-id] {:form/id         form-id
                                :form/definition samples/form-definition
                                :form/state      samples/form-state})}
  (widgets/base
   {:title   (:title definition)
    :toolbar (toolset/toolset (prim/computed props {:events l-cf/form-events}))}
   (dom/div nil (form-fields (prim/computed props
                                            {:fields-defs (:fields-defs definition)
                                             :form-data   (-> state :data)})))))

(def form (prim/factory Form))
