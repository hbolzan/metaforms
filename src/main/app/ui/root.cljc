(ns app.ui.root
  (:require
    [fulcro.client.mutations :as m]
    [fulcro.client.data-fetch :as df]
    #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])

    [app.data-samples.forms :as samples]
    [app.ui.components.complex.form :as c-form]

    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.i18n :as i18n :refer [tr trf]]))

(defsc Root [this {:keys [form]}]
  {:query         [{:form (prim/get-query c-form/Form)}]
   :initial-state (fn [form-id] {:form (prim/get-initial-state c-form/Form :sample)})}
  (c-form/form form))

