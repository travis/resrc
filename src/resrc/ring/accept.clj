(ns resrc.ring.accept
  "Utilities for processing HTTP accepts headers.

These utilities are all experimental and may be modified or moved
in the future.

Use at your own risk."
  (:use resrc.core)
  (:require [clojure.string :as s]))

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

(defn accept-header
  [request]
  ((:headers request) "accept"))

