(ns resrc.core
  "The core Resource abstraction")

(defprotocol Resource
  (GET [resource] [resource request])
  (PUT [resource] [resource request])
  (DELETE [resource] [resource request])
  (POST [resource] [resource request]))
