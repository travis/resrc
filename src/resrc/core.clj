(ns resrc.core
  "The core Resource abstraction")

(defprotocol Resource
  (GET [resource] [resource request])
  (PUT [resource] [resource request])
  (DELETE [resource] [resource request])
  (POST [resource] [resource request]))

(defprotocol Representable
  (represent [resource accepts-list response]
             "Given a resource, a response and an accepts list
this function should transform the response in a manner appropriate
to the most acceptable media type."))
