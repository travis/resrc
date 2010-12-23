(ns resrc.util
  (:use resrc.core))

(defn method-of?
  [protocol method]
  (boolean ((keyword (name method)) (:sigs protocol))))

(defn resource-method?
  [method-spec]
  (method-of? Resource (first method-spec)))



