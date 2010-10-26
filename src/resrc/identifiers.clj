(ns resrc.identifiers
  "Utilities for associating identifiers with resources."
  (:use [clout.core :as clout]))

;;; identifiers

(defn compile-route
  "Given a vector containing a clout path specification and
a resource, return a function that do the following:

Given a path, return vector containing the resource and
any matching path parameters if the path matches the
path spec and nil otherwise.
"
  [[path-spec resource]]
  (let [compiled-path-spec (clout/route-compile path-spec)]
    (fn [path] (when-let [params (clout/route-matches compiled-path-spec path)]
                [resource params]))))

(defn compile-routes
  "Given a vector of [path-spec resource] pairs, returns a function
that will do the following:

Given a path, return a [resource path-params] pair corresponding to the
first matching path spec in routes."
  [routes]
  (let [compiled-routes (map compile-route routes)]
    (fn [path] (some #(% path) compiled-routes))))

(defn reverse-routes
  "Given a vector of [path-spec resource] return a map from resources
to lists of corresponding path-specs"
  [routes]
  (reduce
   (fn [reversed-routes [path resource]]
     (assoc reversed-routes
       resource
       (conj (reversed-routes resource) path)))
   {} routes))

(defn with-reversed-routes
  [val reversed-routes]
  (with-meta val (assoc (meta val) :resrc-reversed-routes reversed-routes)))

(defn reversed-routes
  [val]
  (:resrc-reversed-routes (meta val)))

(defn create-router
  "Given a set of routes, return a function created by compile-routes
with metadata that includes a map from resources to path-specs."
  [routes]
  (with-reversed-routes
    (compile-routes routes)
    (reverse-routes routes)))

(defn path-specs-for-resource
  "Given a router (see create-router) and a resource, return a list of
all path-specs corresponding to this resource."
  [router resource]
  ((reversed-routes router) resource))

(defn path-spec-for-resource
  "Given a router and a resource, return the nth path corresponding to the
resource. n defaults to 0."
  ([router resource n] (nth (path-specs-for-resource router resource) n))
  ([router resource] (path-spec-for-resource router resource 0)))


