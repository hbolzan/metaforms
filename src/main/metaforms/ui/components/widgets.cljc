(ns metaforms.ui.components.widgets
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])))

(defsc Base
  [this {:keys [title toolbar] :as props}]
  (dom/div {:className "col"}
           (dom/div {:className "card"}
                    (dom/div {:className "card-body"}
                             (dom/div {:className "alert alert-secondary"}
                                      (dom/h5 {:className "font-weight-bold"} title)
                                      toolbar)
                             (prim/children this)))))

(def base (prim/factory Base))
