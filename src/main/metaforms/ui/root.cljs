(ns metaforms.ui.root
  (:require
    [fulcro.client.mutations :as m]
    [fulcro.client.data-fetch :as df]
    [fulcro.client.dom :as dom]

    [metaforms.data-samples.forms :as samples]
    [metaforms.ui.components.complex.form :as c-form]

    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.i18n :as i18n :refer [tr trf]]))

(defsc Root [this {:keys [form]}]
  {:query         [{:form (prim/get-query c-form/Form)}]
   :initial-state (fn [form-id] {:form (prim/get-initial-state c-form/Form samples/form-definition)})}
  (c-form/form form))

