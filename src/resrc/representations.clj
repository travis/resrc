(ns resrc.representations
  "Utilities for associating representations with resources.")
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

(defn split-type
  [type]
  [(keyword (namespace type)) (keyword (name type))])

(defn to-representations
  "Turn

 :text/html x :text/plain y

into a list like:

 [[[:text :html] x] [[:text :plain] y]]
"
  [& representations]
  (map (fn [[type representation]] [(split-type type)
                                   representation])
       (partition 2 representations)))

(defn conneg-fn
  "Create a conneg function from two functions - one that extracts an
accepts list from a request and another that sets a content type on a response."
  [accepts-list set-content-type]
  (fn [request response representations]
    (let [[content-type representation]
          (find-acceptable (accepts-list request) representations)]
      (representation (set-content-type response content-type)))))
