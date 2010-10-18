(ns resrc.core
  (:refer-clojure :exclude [get])
  (:use [clout.core :as clout]))

(defprotocol Resource
  (get [resource request])
  (put [resource request])
  (delete [resource request])
  (post [resource request]))

;;; identifiers

(defn find-resource
  [routes path]
  (some (fn [[path-spec resource]]
          (when-let [params (clout/route-matches path-spec path)]
            [resource params]))
        (partition 2 routes)))

;;; representations

(defn component-matches
  [a b]
  (or (= :* a) (= :* b) (= a b)))

(defn type-matches
  [[type-a subtype-a] [type-b subtype-b]]
  (and (component-matches type-a type-b)
       (component-matches subtype-a subtype-b)))

(defn find-representation
  [type representations]
  (some (fn [[representation-type representation]]
          (when (type-matches type representation-type)
            [representation-type representation]))
        representations))

(defn find-acceptable
  "accepts-list should be a preference-ordered list of type/subtype tuples like:
  [[:text :html] [:* :*]]

representations should be a list of type/subtype/representation tuples like:
  [[[:text :plain] 1]
   [[:text :html] 2]
   [[:image :jpeg] 3]]

note that both lists imply preference by their ordering - that is, items earlier
in each list may be considered \"preferable\"
 "
  [accepts-list representations]
  (some (fn [type] (find-representation type representations))
        accepts-list))

(def *representations-key* :resrc-representations)

(defn with-representations
  [obj representations]
  (with-meta obj (assoc (meta obj) *representations-key* representations)))

(defn representations
  [obj]
  (*representations-key* (meta obj)))

;;; synthesis

(defn process-request
  "method should be a method supported by Resource.

Assumes representations are functions from [resource request & rest] to response."
  [routes method path accepts-list request]
  (let [[resource _] (find-resource routes path)]
    (if-let [[_ representation]
             (find-acceptable accepts-list (representations resource))]
      (representation resource (method resource request))
      :not-acceptable)))

;;; sugar

(defn emit-resource-handler
  [[method & forms]]
  `(~method [~'+resource  ~'+request]
            ~@forms))

(defn split-type
  [type]
  [(keyword (namespace type)) (keyword (name type))])

(defn emit-representations
  [representations]
  (apply vector
         (map (fn [[type representation]] [(split-type type)
                                          `(fn [~'+resource ~'+response] ~representation)])
         (partition 2 representations))))


(defmacro resource
  [& args]
  (let [[representations & specs] (reverse args)]
    `(with-representations (reify Resource ~@(map emit-resource-handler specs))
       ~(emit-representations representations))))

;;; faster routing

(defn compile-route
  [[path-spec resource]]
  (let [compiled-path-spec (clout/route-compile path-spec)]
    (fn [path] (when-let [params (clout/route-matches compiled-path-spec path)]
                [resource params]))))

(defn compile-routes
  [routes]
  (let [compiled-routes (map compile-route (partition 2 routes))]
    (fn [path] (some #(% path) compiled-routes))))
