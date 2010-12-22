(ns resrc.ring
  "Utilities for creating ring specific resources.

These utilities are all experimental and may be modified or moved
in the future.

Use at your own risk."
  (:use resrc.core)
  (:require [resrc.representations :as repr]
            [clojure.string :as s]))

(def *not-acceptable-response* {:status 406 :headers {} :body "Could not find acceptable representation."})

(defn emit-resource-handler
  [[method & forms]]
  `(~method [~'+resource {~'+headers :headers
                          ~'+body :body
                          ~'+params (or :params {})
                          :as ~'+request}]
            ~@forms))

(defn split-type
  [type]
  [(keyword (namespace type)) (keyword (name type))])

(defn emit-representations
  [representations]
  (apply vector
         (map (fn [[type representation]] [(split-type type)
                                          `(fn [~'+response] ~representation)])
              (partition 2 representations))))

(defn add-content-type
  [response type-vector]
  (assoc-in response
            [:headers "Content-Type"]
            (s/join "/" (map name type-vector))))

(defn apply-representation [representations accepts-list response]
  (if-let [[response-type representation]
           (repr/find-acceptable accepts-list representations)]
    (add-content-type (representation response) response-type)
    *not-acceptable-response*))

(defn process-request
  [resource request]
  (assoc ((ns-resolve 'resrc.core (symbol (s/upper-case (name (:request-method request)))))
          resource request)
    :resource resource))

(defmacro resource
  "Usage:  (resource method-implementations+ representations?)

Create a resource by specifying implementations for the Resource and
Representable protocols.

Bindings for each method-implementation will be automatically created:
Commonly used parameters in the Ring request will be bound to variables
prefixed by +. The request itself will also be bound in this fasion.

Bound parameters include:

+request
+headers
+body
+params

representations should be a vector of alternating media types and
representation implementations. The response will be available
in these implementations as +response.

Examples

 (resource (GET {:status 200
                 :body \"hello\"}))

 (resource (GET {:status 200
                 :body (str \"hello \" (+params \"name\"))}))

 (resource (GET {:status 200
                 :body \"hello\"})
           [:text/plain +response
            :text/html (assoc +response
                         :body (str \"<html><body>\"
                                    (:body +response)
                                    \"</body></html>\"))])
"
  [& args]
  (let [[representations & specs] (reverse args)
        [representations specs] (if (vector? representations)
                                  [representations specs]
                                  ['[:*/* +response] (cons representations specs)])]
    `(reify
      Resource
      ~@(map emit-resource-handler specs)
      Representable
      (represent [~'+resource accepts-list# ~'+response]
                 (apply-representation
                  ~(emit-representations representations)
                  accepts-list# ~'+response))
      ;; for convenience, make a resource behave like a function
      clojure.lang.IFn
      (invoke [resource# request#] (process-request resource# request#)))))

(defn parse-accept-params
  "params is a seq of params like 'q=0.8' or 'level=1'"
  [params]
  (apply hash-map
         (apply concat
                (map #(let [[k v] (s/split % #"=")] [(keyword k) v]) params))))

(defn add-accept-metadata
  "accepts-type is a vector like [:text :plain]
params is a map like {:q 0.8 :level 1}"
     [accept-type params]
     (with-meta accept-type
       {:accept-q (Double. (or (:q params) "1"))}))

(defn parse-accept-component
  "component is a string like text/plain;q=0.8;level=1"
  [component]
  (let [[content-type & params] (s/split component #";")
        [type subtype] (s/split content-type #"/")
        params-map (parse-accept-params params)]
    (add-accept-metadata [(keyword type) (keyword subtype)] params-map)))

(defn sort-by-q-value
  "accepts is a seq of accept types like [[:text :plain] [:text :html]]

each item in the seq must have an associated :accept-q metadatum"
  [accepts]
  (sort-by #(- 1.0 (:accept-q (meta %))) accepts))

(defn parse-accept
  "accepts-string is a string like
text/*, text/html, text/html;level=1, */*"
  [accept-string]
  (sort-by-q-value
   (map #(parse-accept-component (s/trim %))
        (s/split (or accept-string "") #","))))

(defn wrap-represent
  "Assumes representations are functions from [resource request] to response."
  [app]
  (fn [request]
    (let [response (app request)
          resource (:resource response)
          representations (:representations response)]
      (if (satisfies? Representable resource)
        (represent resource
                   (parse-accept (or ((:headers request) "accept") "*/*"))
                   response)
        response))))

(defn request-processor
  [router]
  (fn [request]
    (let [[resource path-params] (router (:uri request))]
      (process-request
       resource
       (merge request
              {:path-params path-params
               :params (merge (:params request) path-params)})))))

(defn ring-handler
  [router]
  (-> (request-processor router)
      wrap-represent))
