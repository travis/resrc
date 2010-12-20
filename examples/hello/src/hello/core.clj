(ns hello.core
  (:use [resrc core identifiers ring]
        [ring.adapter.jetty :only [run-jetty]])
  (:require [resrc.client :as client]))

(def hello-server-resrc (resource (GET {:status 200 :body "hello"})
                                  [:text/plain +response]))

(def hello-travis-server-resrc (resource (GET {:status 200 :body "hello travis"})
                                          [:text/plain +response]))

(def app (ring-handler
          (create-router
           [["/hello" hello-server-resrc]
            ["/hello/travis" hello-travis-server-resrc]])))

(defn -main [] (run-jetty #'app {:port 8080}))

(def hello-client-resrc (client/resource "http://localhost:8080/hello"))
(def hello-travis-client-resrc (client/subresource hello-client-resrc "travis"))

(comment
  (GET hello-client-resrc)
  (GET hello-travis-client-resrc))
