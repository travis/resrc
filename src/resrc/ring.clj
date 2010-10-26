(ns resrc.ring
  "Utilities for creating ring specific resources.

These utilities are all experimental and may be modified or moved
in the future.

Use at your own risk."
  (:require [resrc.core :as core]
            [resrc.util :as util]
            [resrc.representations :as repr]
            [clojure.string :as s]))


(defn emit-resource-handler
  [[method & forms]]
  `(~method [~'+resource {~'+server-port :server-port
                          ~'+server-name :server-name
                          ~'+remote-addr :remote-addr
                          ~'+uri :uri
                          ~'+query-string :query-string
                          ~'+scheme :scheme
                          ~'+request-method :request-method
                          ~'+content-type :content-type
                          ~'+content-length :content-length
                          ~'+character-encoding :character-encoding
                          ~'+headers :headers
                          ~'+body :body
                          :as ~'+request}]
            ~@forms))

;;(emit-resource-handler '(get (prn (+query-string))))

(defmacro resource
  [& args]
  (let [[representations & specs] (reverse args)]
    `(repr/with-representations
       (reify resrc.core/Resource ~@(map emit-resource-handler specs))
       ~(util/emit-representations representations))))

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
  (let [[content-type & params] (s/split component #";")]
    (let [[type subtype] (s/split content-type #"/")
          params-map (parse-accept-params params)]
      (add-accept-metadata [(keyword type) (keyword subtype)] params-map))))

(defn sort-by-q-value
  "accepts is a seq of accept types like [[:text :plainn] [:text :html]]

each item in the seq must have an associated :accept-q metadatum"
  [accepts]
  (sort-by #(- 1.0 (:accept-q (meta %))) accepts))

(defn parse-accept
  "accepts-string is a string like
text/*, text/html, text/html;level=1, */*"
  [accept-string]
  (sort-by-q-value
   (map #(parse-accept-component (s/trim %))
        (s/split accept-string #","))))

(defn handle-not-acceptable
  [response]
  (if (= :not-acceptable response)
    {:status 405
     :headers {}}
      response))

(defn add-content-type
  [response type-vector]
  (assoc response
    :headers
    (assoc (:headers response)
      "Content-Type" (s/join "/" (map name type-vector)))))

(defn process-request
  "method should be a method supported by Resource.

Assumes representations are functions from [resource request & rest] to response."
  [router request]
  (let [path (:uri request)
        [resource path-params] (router path)
        method (ns-resolve 'resrc.core (symbol (s/upper-case (name (:method request)))))
        accepts-list (parse-accept ((:headers request) "Accept"))]
    (if-let [[response-type representation]
             (repr/find-acceptable accepts-list (repr/representations resource))]
      (add-content-type
       (representation
        resource
        (method resource (assoc request :path-params path-params)))
       response-type)
      {:status 405 :headers {}})))

(defn ring-handler
  [router]
  #(process-request router %))
