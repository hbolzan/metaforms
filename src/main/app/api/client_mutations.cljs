(ns app.api.client-mutations
  (:require
   [fulcro.client.mutations :refer [defmutation]]
   [fulcro.client.logging :as log]))

(defmutation mutate-form
  [{:keys [form-mutation-fn]}]
  (action [{:keys [state]}]
          (let [old-form (:form @state)
                new-form (form-mutation-fn old-form)])
          (swap! state assoc :form new-form)))
