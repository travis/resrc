(ns resrc.util
  (:use resrc.core)
  (:require [clojure.contrib.string :as s]))

(defn method-of?
  [protocol method]
  (boolean ((keyword (name method)) (:sigs protocol))))

(defn resource-method?
  [method-spec]
  (method-of? Resource (first method-spec)))

(defn wrapper-method-impl
  [#^java.lang.reflect.Method method inner]
  (let [name (symbol (.getName method))
        params (map-indexed (fn [i _] (gensym i)) (.getParameterTypes method))]
    `(~name [this# ~@params] (. (. this# ~inner) ~name ~@params))))

(defn arity-index
  [defs]
  (reduce
   (fn [h [name args & rest :as def]] (assoc h (str name "/" (count args)) def))
   {} defs))

(defn merge-method-defs
  [a b]
  (map second
   (merge
    (arity-index a)
    (arity-index b))))

(defn iface-wrappers
  [iface inner]
  (map #(wrapper-method-impl % inner) (.getMethods iface)))

(defmacro defwrappertype
  "Define a type that takes an argument and proxies calls to that argument.
Users may define alternate implementations that do not proxy calls."
  [name [wrapper-sym & _ :as args] wrapped & impls]
  {:pre [(not (nil? wrapper-sym))]}
  `(deftype ~name ~args
     ~wrapped
     ~@(merge-method-defs
        (iface-wrappers (resolve wrapped) wrapper-sym)
        impls)))
