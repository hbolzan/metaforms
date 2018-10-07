(ns app.ui.components.complex.form
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [app.data-samples.forms :as samples]
            [app.ui.logic.complex-forms :as l-cf]
            [app.ui.logic.inputs :as l-i]
            [app.ui.components.widgets :as widgets]
            [app.ui.components.complex.inputs :as w-i]
            [app.ui.components.complex.toolset :as toolset]
            [app.ui.components.complex.types :as types]
            #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])))

(defsc Field
  [this {:keys [f-def]} {:keys [field-def additional-group-class]}]
  (dom/div {:className (str "form-group" (some->> additional-group-class (str " ")))}
           (dom/label {:htmlFor (l-i/field-id field-def)} (:label field-def))
           (w-i/input (prim/computed {} {:field-def field-def}))))

(def field (prim/factory Field {:keyfn #(-> (:f-def %) l-i/field-id)}))

(defn width->col-md-class [width]
  (str "col-md-" width))

(defsc Row
  [this {:keys [row-index]} {:keys [row-def]}]
  (if (= (count (:defs row-def)) 1)
    (field (prim/computed {:f-def (-> row-def :defs first)} {:field-def (-> row-def :defs first)}))
    (dom/div {:className "form-row"}
             (map (fn [field-def bootstrap-width]
                    (field (prim/computed {:f-def field-def}
                                          {:field-def              field-def
                                           :additional-group-class bootstrap-width})))
                  (:defs row-def)
                  (map width->col-md-class (:bootstrap-widths row-def))))))

(def row (prim/factory Row {:keyfn :row-index}))

(defsc FormFields
  [this _ {:keys [fields-defs form-data]}]
  (let [rows-defs (l-cf/distribute-fields (l-cf/defs<-data fields-defs form-data) l-cf/bootstrap-md-width)]
    (map-indexed (fn [index row-def] (row (prim/computed {:row-index index} {:row-def row-def}))) rows-defs)))

(def form-fields (prim/factory FormFields))

(defsc Form
  [this {:form/keys [id definition state] :as form}]
  {:ident         [:form/by-id :form/id]
   :query         [:form/id :form/definition :form/state]
   :initial-state (fn [form-id] {:form/id         form-id
                                 :form/definition samples/form-definition
                                 :form/state      samples/form-state})}
  (widgets/base
   {:title   (:title definition)
    :toolbar (toolset/toolset (prim/computed {:form form} {:events l-cf/form-events}))}
   (dom/div nil (form-fields (prim/computed {} {:fields-defs (:fields-defs definition)
                                                :form-data   (-> state :data)})))))

(def form (prim/factory Form))

;; (defsc FormList
;;   [this {:form-list/keys [group forms visible]}]
;;   {:query         [:form-list/group {:form-list/forms (prim/get-query Form)}]
;;    :initial-state (fn [{:keys [group visible]}]
;;                     {:form-list/group   group
;;                      :form-list/visible visible
;;                      :form-list/forms   [(prim/get-initial-state Form {:id :sample})]})}
;;   (form (filter #(= {:id %} visible) forms)))

;; (def form-list (prim/factory FormList))
