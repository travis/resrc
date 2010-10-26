(ns resrc.core
  "The core Resource abstraction")

(defprotocol Resource
  (GET [resource request])
  (PUT [resource request])
  (DELETE [resource request])
  (POST [resource request]))
