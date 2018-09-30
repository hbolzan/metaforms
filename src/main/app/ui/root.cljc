(ns app.ui.root
  (:require
    [fulcro.client.mutations :as m]
    [fulcro.client.data-fetch :as df]
    #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])
    [app.api.mutations :as api]

    [app.data-samples.forms :as samples]
    [app.ui.components.complex.form :as c-form]

    [fulcro.client.primitives :as prim :refer [defsc]]
    [fulcro.i18n :as i18n :refer [tr trf]]))

;; The main UI of your application

(defsc Root [this props]
  (c-form/form {:form-definition samples/form-definition
                :form-state      samples/form-state}))