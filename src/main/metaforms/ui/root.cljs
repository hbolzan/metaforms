(ns metaforms.ui.root
  (:require
    [fulcro.client.mutations :as m]
    [fulcro.client.data-fetch :as df]
    [fulcro.client.dom :as dom]

    [metaforms.data-samples.forms :as samples]
    [metaforms.ui.components.complex.form :as c-form]
    [metaforms.ui.components.dialogs :as dialogs]

    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.i18n :as i18n :refer [tr trf]]))

(defsc Root [this {:keys [form modal]}]
  {:query         [{:form (prim/get-query c-form/Form)}
                   {:modal (prim/get-query dialogs/ModalDialog)}]
   :initial-state (fn
                    [form-id]
                    {:form  (prim/get-initial-state c-form/Form
                                                    {:form-definition samples/form-definition
                                                     :dataset         samples/dataset})
                     :modal (prim/get-initial-state dialogs/ModalDialog {})})}
  (dom/div nil
           (c-form/form form)
           (dialogs/modal-dialog modal)))

