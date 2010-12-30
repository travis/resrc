(ns resrc.ring
  "Utilities for creating ring specific resources.

These utilities are all experimental and may be modified or moved
in the future.

Use at your own risk."
  (:use resrc.core
        [resrc.util :only [resource-method?]])
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

(defn emit-representations
  [representations]
  (apply vector
         (map (fn [[type representation]]
                [type `(fn [~'+response] ~representation)])
              (apply repr/to-representations representations))))

(defn add-content-type
  [response type-vector]
  (assoc-in response
            [:headers "Content-Type"]
            (s/join "/" (map name type-vector))))

(defn mod-body
  [f]
  (fn [response] (assoc response :body (f (:body response)))))

(defn apply-representation [representations accepts-list response]
  "Applies a representation function from representations
to response based on accepts-list.

representations is a vector of two item vectors consisting of a media type
vector and a function transforming a response like:
 [[:text :plain] (fn [response] response)]

accepts-list is a vector of acceptable media types. Each item in the vector
is a media type vector, and may include wildcards.

response is the ring response being transformed

Returns a 406 - Not Acceptable response if no match can be found.
"
  (if-let [[response-type representation]
           (repr/find-acceptable accepts-list representations)]
    (add-content-type (representation response) response-type)
    *not-acceptable-response*))

(defn represent-fn-from
  [& representations]
  (fn [resource accepts response]
    (apply-representation
     (apply repr/to-representations representations)
     accepts
     response)))

(defn body-as
  [& representations]
  (apply vector
   (apply concat
          (map (fn [[k f]] [k (mod-body f)]) (partition 2 representations)))))

(defn represent-body-fn-from
  [& representations]
  (apply represent-fn-from
         (apply body-as representations)))

(defn- resource-representable-specs
  [args]
  (let [[representations & specs] (reverse args)
        [representations specs]
        (if (not (resource-method? representations))
          [representations specs]
          ['[:*/* identity] (cons representations specs)])]
    `(Resource
      ~@(map emit-resource-handler specs)
      Representable
      (represent [resource# accepts-list# response#]
                 ((apply represent-fn-from ~representations)
                  resource# accepts-list# response#)))))

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
  `(reify
    ~@(resource-representable-specs args)
    ;; for convenience, make a resource behave like a function
    clojure.lang.IFn
    (invoke [resource# request#] (process-request resource# request#))))

(defmacro defresource
  [name fields & args]
  `(deftype ~name ~fields
     ~@(resource-representable-specs args)))

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
