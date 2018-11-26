(ns metaforms.ui.components.dialogs
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client.mutations :as fm :refer [defmutation]]
            [fulcro.client.dom :as dom]
            [fulcro.ui.html-entities :as entities]))

(defsc ModalDialog
  [this
   {:modal/keys [id visible? title body-content on-confirm] :as props}]
  {:ident          [:modal/by-id :modal/id]
   :initial-state  (fn [_] {:modal/id           (random-uuid)
                           :modal/visible?     false
                           :modal/title        "Confirmation"
                           :modal/body-content "Are you sure?"
                           :modal/on-confirm   nil})
   :initLocalState (fn [_] {:close #(fm/toggle! this :modal/visible?)})
   :query          [:modal/id
                    :modal/visible?
                    :modal/title
                    :modal/body-content
                    :modal/on-confirm]}
  (def modal-singleton this)
  (dom/div
   nil
   (dom/div {:className (if visible? "show modal fade" "modal fade")
             :tab-index -1
             :role      "dialog"
             :style     (merge {:display      "block"
                                :paddingRight "14px"}
                               (when-not visible? {:zIndex -9999}))}
            (dom/div {:className "modal-dialog" :role "document"}
                     (dom/div {:className "modal-content"}
                              (dom/div {:className "modal-header"}
                                       (dom/h5 {:className "modal-title"} title)
                                       (dom/button {:type         "button"
                                                    :className    "close"
                                                    :data-dismiss "close"
                                                    :aria-label   "Close"
                                                    :onClick      (prim/get-state this :close)}
                                                   (dom/span {:aria-hidden true} entities/times)))

                              (dom/div {:className "modal-body"}
                                       (dom/p nil body-content))

                              (dom/div {:className "modal-footer"}
                                       (dom/button {:type         "button"
                                                    :className    "btn btn-secondary"
                                                    :data-dismiss "modal"
                                                    :onClick      (prim/get-state this :close)}
                                                   "Close")
                                       (dom/button {:type      "button"
                                                    :className "btn btn-primary"
                                                    :onClick   #(((prim/get-state this :close))
                                                                 (on-confirm))}
                                                   "Confirm")))))

   ;; modal overlay
   (dom/div {:className (if visible? "show modal-backdrop fade" "modal-backdrop fade")
             :style     (when-not visible? {:zIndex -9999})})))

(def modal-dialog (prim/factory ModalDialog))

(defn confirmation-dialog [title content on-confirm]
  (prim/compressible-transact! modal-singleton `[(fm/set-props ~{:modal/visible?     true
                                                                 :modal/title        title
                                                                 :modal/body-content content
                                                                 :modal/on-confirm   on-confirm})]))
