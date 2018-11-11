(ns metaforms.ui.components.complex.form
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client.mutations :as m :refer [defmutation]]
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
   {:keys [additional-group-class form-on-change]}]
  {:ident         [:field/by-id :field/id]
   :initial-state (fn
                    [{:keys [form-id name label field-kind read-only data-type width options data-fields] :as field-def}]
                    (let [data-field (first (filter #(= (:data-field/name %) name) data-fields))]
                      {:field/id        (keyword form-id name)
                       :field/name      name
                       :field/label     label
                       :field/kind      field-kind
                       :field/read-only read-only
                       :field/data-type data-type
                       :field/width     width
                       :field/options   options
                       :field/value     (or (:data-field/value data-field) "")}))
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
                                  :on-change  (fn [evt]
                                                (m/set-string! this :field/value :event evt)
                                                (when form-on-change (form-on-change name evt)))
                                  :read-only  read-only})))

(def form-field (prim/factory FormField {:keyfn :field/name}))

(defmutation set-field-value [{:keys [field-id value]}]
  (action [{:keys [state]}]
          (swap! state assoc-in [:field/by-id field-id :field/value] value)))

(defn get-current-record [state form-id]
  (get-in @state
          (get-in @state
                  (conj (get-in @state [:form/by-id form-id :form/dataset]) :dataset/current-record))))

(defn data-record->data-fields [state data-record]
  (mapv #(get-in @state %) (:data-record/fields data-record)))

(defn form-sync-data [state form-fields data-fields]
  (doseq [field-ident form-fields]
    (let [form-field (get-in @state field-ident)
          field-id   (:field/id form-field)
          field-name (:field/name form-field)
          data-field (first (filter #(= (:data-field/name %) field-name) data-fields))
          value      (or (:data-field/value data-field) "")]
      (swap! state assoc-in [:field/by-id field-id :field/value] value))))

(defn record-sync-data [state form-fields data-fields]
  (doseq [field-ident form-fields]
    (let [form-field (get-in @state field-ident)
          data-field (first (filter #(= (:data-field/name %) (:field/name form-field)) data-fields))
          value      (:field/value form-field)]
      (swap! state assoc-in [:data-field/by-id (:data-field/id data-field) :data-field/value] value))))

(defmutation do-set-form-state
  "checks current form/state before changing"
  [{:keys [form-id new-state]}]
  (action [{:keys [state]}]
          (when-not (= new-state (get-in @state [:form/by-id form-id :form/state]))
            (let [form-ident [:form/by-id form-id]]
              (swap! state assoc-in [:form/by-id form-id :form/state] new-state)))))

(defmutation do-form-discard [{:keys [form-id]}]
  (action [{:keys [state]}]
          (let [current-record (get-current-record state form-id)
                form-fields    (get-in @state [:form/by-id form-id :form/fields])
                data-fields    (data-record->data-fields state current-record)]
            (form-sync-data state form-fields data-fields)
            (swap! state assoc-in [:form/by-id form-id :form/state] (if (empty? current-record) :empty :view)))))

(defmutation do-form-confirm [{:keys [form-id]}]
  (action [{:keys [state]}]
          (let [current-record (get-current-record state form-id)
                form-fields    (get-in @state [:form/by-id form-id :form/fields])
                data-fields    (data-record->data-fields state current-record)]
            (record-sync-data state form-fields data-fields)
            (swap! state assoc-in [:form/by-id form-id :form/state] :view))))

(defn set-form-state [new-state form-id component]
  (prim/transact! component `[(do-set-form-state {:form-id ~form-id :new-state ~new-state})]))

(defn form-on-fields-change [component form-id field-name event]
  (set-form-state :edit form-id component))

(def form-append (partial set-form-state :edit))
(def form-delete (partial set-form-state :empty))
(def form-edit (partial set-form-state :edit))
#_(def form-confirm (partial set-form-state :view))

(defn form-confirm [form-id component]
  (prim/transact! component `[(do-form-confirm {:form-id ~form-id})]))

(defn form-discard [form-id component]
  (prim/transact! component `[(do-form-discard {:form-id ~form-id})]))

(defn form-events [component form-id]
  {:append  #(form-append form-id %)
   :delete  #(form-delete form-id %)
   :edit    #(form-edit form-id %)
   :confirm #(form-confirm form-id %)
   :discard #(form-discard form-id %)})

(defn dataset-events []
  {:after-scroll  nil
   :after-insert  nil
   :after-delete  nil
   :after-refresh nil})

(defn form-row
  "renders fields distributed in a row"
  [component form-id row-index row-def fields]
  (dom/div
   {:className "form-row" :key (str "row-" row-index)}
   (map (fn
          [field bootstrap-width]
          (form-field (prim/computed
                       field {:additional-group-class (l-cf/width->col-md-class bootstrap-width)
                              :form-on-change         (partial form-on-fields-change component form-id)})))
        (l-cf/row-fields row-def fields)
        (:bootstrap-widths row-def))))

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
                    [{{fields-defs :fields-defs form-id :id} :form-definition :as form-definition
                      dataset-def                            :dataset}]
                    (let [dataset (prim/get-initial-state
                                   data/DataSet
                                   (assoc dataset-def
                                          :fields-defs fields-defs
                                          :events (dataset-events)))]
                      {:form/id        form-id
                       :form/title     (:title form-definition)
                       :form/state     (if (empty? (:dataset/records dataset)) :empty :view)
                       :form/fields    (mapv #(prim/get-initial-state
                                               FormField
                                               (assoc % :form-id form-id :data-fields (-> dataset :dataset/current-record :data-record/fields))) fields-defs)
                       :form/rows-defs (l-cf/distribute-fields fields-defs l-cf/bootstrap-md-width)
                       :form/dataset   dataset}))}
  (widgets/base
   {:title   title
    :toolbar (toolset/toolset (prim/computed props {:events (form-events this id)}))}
   (dom/div nil (map-indexed (fn [index row-def] (form-row this id index row-def fields)) rows-defs))))

(def form (prim/factory Form))
