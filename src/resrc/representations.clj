(ns resrc.representations
  "Utilities for associating representations with resources.

As currently defined, these expect Resources that support metadata.")
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


(defn with-representations
  [obj representations]
  (with-meta obj (assoc (meta obj) :resrc-representations representations)))

(defn representations
  [obj]
  (:resrc-representations (meta obj)))

