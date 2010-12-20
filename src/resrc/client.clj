(ns resrc.client
  "A resource oriented HTTP client library."
  (:use [clojure.contrib.io :only [as-url]])
  (:require resrc.core
            [clj-http.client :as http])
  (:import java.net.URI))

(defn request
  [resource method req]
  ((.adapter resource) (merge req {:request-method method
                                   :url (.toString (.normalize (.url resource)))})))
(deftype Resource [^URI url adapter]
  resrc.core.Resource
  (GET [resource req] (request resource :get req))
  (PUT [resource req] (request resource :put req))
  (DELETE [resource req] (request resource :delete req))
  (POST [resource req] (request resource :post req))

  (GET [resource] (request resource :get {}))
  (PUT [resource] (request resource :put {}))
  (DELETE [resource] (request resource :delete {}))
  (POST [resource] (request resource :post {})))

(defn resource
  ([url http-adapter]
     (Resource. (URI. url) http-adapter))
  ([url] (resource url http/request)))

(defn subresource
  [resource path]
  (Resource.  (URI. (str (.url resource) "/" path))
              (.adapter resource)))

