(ns resrc.ring
  "Utilities for creating ring specific resources.

These utilities are all experimental and may be modified or moved
in the future.

Use at your own risk."
  (:use resrc.core
        [resrc.ring.accept :only [parse-accept accept-header]])
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

(def conneg
     (repr/conneg-fn #(parse-accept (accept-header %))
                     #(assoc-in %1 [:headers "Content-Type"] (s/join \/ (map name %2)))
                     (constantly {:status 406 :headers {}})))

(defmacro defrepresentation
  [name args & impls]
  (repr/emit-defrepresentation 'resrc.ring/conneg name args impls))

(defn mod-body
  [f]
  (fn [response] (assoc response :body (f (:body response)))))

(defn body-as
  [& representations]
  (apply vector
   (apply concat
          (map (fn [[k f]] [k (mod-body f)]) (partition 2 representations)))))
