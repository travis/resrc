# resrc

resrc is a Clojure library for creating RESTful APIs

## Usage

resrc is a collection of tools for creating APIs in the REST
architectural style. The most important piece of this library is the
Resource protocol defined in resrc.core. It declares the core of
REST's uniform interface - GET, PUT, POST, and DELETE.

Implementors can easily extend the Resource protocol to their
datatypes, like:

    (extend-protocol Resource
      clojure.lang.APersistentMap
      (GET [hash request] (get hash request)))

## Open Questions

* Should Resource include additional HTTP methods like HEAD, OPTIONS,
  HEAD, TRACE, CONNECT and PATCH?

* Do utilities like the ones found in util.clj belong in this library?

* How do we support associating representations in a general way that
  does not require metadata, since metadata cannot be added to all
  objects?

## Installation

lein install resrc 0.0.1

## License

Copyright (C) 2010 Travis Vachon

Distributed under the Eclipse Public License, the same as Clojure.
