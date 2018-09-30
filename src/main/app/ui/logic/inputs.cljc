(ns app.ui.logic.inputs)

(defn field-id [field-def]
  (str "field-id-" (:name field-def)))
