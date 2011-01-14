(ns resrc.core
  "The core Resource abstraction")

(defprotocol Resource
  (GET [resource] [resource request])
  (HEAD [resource] [resource request])
  (PUT [resource] [resource request])
  (DELETE [resource] [resource request])
  (POST [resource] [resource request])

  (OPTIONS [resource] [resource request])
  (TRACE [resource] [resource request])
  (CONNECT [resource] [resource request]))
