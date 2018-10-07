(ns app.api.mutations
  (:require
   [fulcro.client.mutations :refer [defmutation]]
   [fulcro.client.logging :as log]))

(defmutation mutate-form
  [{:keys [form-mutation-fn]}]
  (action [{:keys [state]}]
          (let [old-form (get-in @state [:form/by-id :sample])
                new-form (form-mutation-fn old-form)]
            (swap! state assoc-in [:form/by-id :sample] new-form))))

