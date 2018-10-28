(ns app.ui.logic.complex-forms
  (:require [fulcro.client.mutations :refer [defmutation]]
            [fulcro.client.primitives :as prim]
            [app.api.mutations :as api]))

(def empty-row {:width 0 :widths []})
(def field-width-multiplier 10)
(def bootstrap-grid-cols 12)
(def bootstrap-md-width 720)

(defn set-last [coll x]
  (if (< (count coll) 1)
    [x]
    (conj (pop coll) x)))

(defn add-width-to-row [row width]
  (merge row {:width  (+ (:width row) width)
              :widths (conj (:widths row) width)}))

(defn add-to-row? [row width container-width]
  (boolean (and (< (count (:widths row)) bootstrap-grid-cols)
                (<= (+ (:width row) width) container-width))))

(defn row-reducer [container-width rows width]
  (let [current-row (last rows)]
    (if (add-to-row? current-row width container-width)
      (set-last rows (add-width-to-row current-row width))
      (conj rows {:width width :widths [width]}))))

(defn distribute-widths [widths container-width]
  (reduce (partial row-reducer container-width) [empty-row] widths))

(defn row-widths->grid-widths [grid-size row]
  (let [row-width (:width row)
        rate      (/ row-width grid-size)]
    (map (fn [width] (-> (/ width rate) double Math/round)) (:widths row))))

(defn assoc-bootstrap-widths [row bootstrap-widths]
  (assoc row :bootstrap-widths bootstrap-widths))

(defn final-rows->final-defs [final-rows fields-defs]
  (reduce (fn [{output :output next-rows :rest :as results} row]
            {:output (into output [(merge row {:defs (take (count (:widths row)) next-rows)})])
             :rest   (drop (count (:widths row)) next-rows)})
          {:output [] :rest fields-defs}
          final-rows))

(defn row-def-defs->fields [row-def]
  (let [names (map :name (:defs row-def))]
    (-> row-def (dissoc :defs) (assoc :fields names))))

(defn distribute-fields [fields-defs container-width]
  (let [distributed-rows      (distribute-widths (map :width fields-defs) (/ container-width field-width-multiplier))
        bootstrap-widths-rows (map (partial row-widths->grid-widths bootstrap-grid-cols) distributed-rows)
        final-rows            (map assoc-bootstrap-widths distributed-rows bootstrap-widths-rows)]
    (mapv row-def-defs->fields (:output (final-rows->final-defs final-rows fields-defs)))))

(defn defs<-data [fields-defs data]
  (map #(assoc % :value (get data (:name %))) fields-defs))

(def empty-by-type {:char    ""
                    :integer 0
                    :float   0.0})

(defn new-for-type [field-def]
  (let [data-type (:data-type field-def)]
    (if data-type (data-type empty-by-type) "")))

(defn field-change [form field-name value]
  (assoc-in form [:form/state :data field-name] value))

(defn new-data [fields-defs]
  (into {}
        (map
         (fn [def] {(:name def) (or (:default def) (new-for-type def))})
         fields-defs)))

(defn append [form]
  (assoc form :form/state {:state    :edit
                           :data     (-> form :form/definition :fields-defs new-data)
                           :old-data (-> form :form/state :data)}))

(defn old-data [form]
  (let [old (-> form :form/state :old-data)]
    (if old
      old
      (-> form :form/definition :fields-defs new-data))))

(defn discard [form]
  (assoc form :form/state {:state :view
                           :data  (old-data form)}))
(defn form-state-change [form]
  (let [fields-defs (-> form :form/definition :fields-defs)
        form-data   (-> form :form/state :data)
        rows-defs   (distribute-fields (defs<-data fields-defs form-data) bootstrap-md-width)]
    (update form :form/state #(merge % {:rows-defs rows-defs}))))

(defn produce-event [handler]
  (fn [component]
    (prim/transact! component `[(app.api.mutations/mutate-form {:form-id          :sample
                                                                :form-mutation-fn ~(comp form-state-change handler)})])))

(defn on-change-field
  [component field-name value]
  (let [new-form (fn [form] (field-change form field-name value))]
    (prim/transact! component `[(app.api.mutations/mutate-form {:form-id          :sample
                                                                :form-mutation-fn ~new-form})])))

(def form-events {:onAppend (produce-event append)
                  :onDiscard (produce-event discard)})
