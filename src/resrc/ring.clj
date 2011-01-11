(ns resrc.ring
  "Utilities for creating ring specific resources.

These utilities are all experimental and may be modified or moved
in the future.

Use at your own risk."
  (:use resrc.core)
  (:require [resrc.representations :as repr]
            [clojure.string :as s]))

(defn process-request
  [resource request]
  ((ns-resolve 'resrc.core (symbol (s/upper-case (name (:request-method request)))))
   resource request))

(defn handler
  [router]
  (fn [request]
    (let [[resource path-params] (router (:uri request))]
      (process-request
       resource
       (merge request
              {:path-params path-params
               :params (merge (:params request) path-params)})))))
