(ns resrc.util
  "Experimental module for utilities. All of the functions
in this library may be moved or changed significantly in the future.

Use at your own risk."
  (:use [resrc core identifiers representations]))

;;; synthesis

(defn process-request
  "Basic method for processing a request.

method should be a method supported by Resource.

Assumes representations are functions from [resource request & rest] to
response.

Discards extracted path parameter and response content type information."
  [router method path accepts-list request]
  (let [[resource _] (router path)]
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
  "experimental macro "
  [& args]
  (let [[representations & specs] (reverse args)]
    `(with-representations (reify Resource ~@(map emit-resource-handler specs))
       ~(emit-representations representations))))

