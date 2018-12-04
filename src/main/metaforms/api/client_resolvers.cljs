(ns metaforms.api.client-resolvers
  (:require
   [com.wsscode.pathom.connect :as pc]
   [metaforms.ui.logic.complex-forms :as l-cf]
   [metaforms.data-samples.forms :as samples]))

(defn form-definition->form [{:keys [form-id dataset-name title fields-defs]}]
  {:form/id           form-id
   :form/dataset-name dataset-name
   :form/title        title
   ;; :form/state     (if (empty? (:data-rows dataset)) :empty :view)
   ;; :form/fields
   :form/rows-defs    (l-cf/distribute-fields fields-defs l-cf/bootstrap-md-width)
   ;; :form/dataset   dataset
   })

(defn form-parser [form-id]
  ;; TODO: get form definition for form-id from forms API
  (form-definition->form samples/form-definition))

(pc/defresolver form-resolver [_ {:keys [form/id]}]
  {::pc/input #{:form/id}
   ::pc/output [:form/id :form/dataset-name :form/title :form/rows-defs]}
  (form-parser id))

(comment
  (form-parser :sample))


(defn dataset-parser [dataset-id]
  (let [{:keys [name id-key fields-defs data-rows] :as dataset} samples/dataset]
    :dataset/name  name
    :dataset/id-key id-key
    :dataset/state :browse
    ))

(pc/defresolver form-dataset-resolver [_ {:keys [form/id]}]
  :dataset/
  {::pc/input #{:form/id :form/dataset-id}})
