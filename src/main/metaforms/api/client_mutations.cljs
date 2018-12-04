(ns metaforms.api.client-mutations
  (:require
   [fulcro.client.mutations :refer [defmutation]]
   [fulcro.client.logging :as log]))

(defmutation mutate-form
  [{:keys [form-mutation-fn]}]
  (action [{:keys [state]}]
          (swap! state update-in [:form] form-mutation-fn)))
