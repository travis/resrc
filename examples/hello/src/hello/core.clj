(ns hello.core
  (:use [resrc core identifiers ring]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.params :only [wrap-params]]
        [ring.util.response :only [response]]
        [hiccup.core :only [html]])
  (:require [resrc-client.core :as client]
            [resrc.representations :as repr]))

(extend-type Object
  Resource
  (GET [o req] (response (.toString o))))

(extend-type java.util.Map
  Resource
  (GET [m req] (response m)))

(deftype hello-server-resrc []
  Resource
  (GET [_ _] (response "hello")))

(deftype hello-name-server-resrc []
  Resource
  (GET [_ request] (response (str "hello " ((:params request) "name"))))
  )

(defrepresentation hello-name-server-repr [resrc]
    (GET [_ _] (body-as
                :text/plain identity
                :text/html (fn [body] (html [:div body])))))

(def app (-> (handler
              (create-router
               [["/hello" (hello-server-resrc.)]
                ["/hello/:name" (hello-name-server-repr.
                                 (hello-name-server-resrc.))]
                ["/class" Class]
                ["/map" {:a :b :c :d}]]))
             wrap-params))

(defn -main [] (run-jetty #'app {:port 8080}))

(def hello-client-resrc (client/resource "http://localhost:8080/hello"))
(def hello-travis-client-resrc (client/subresource hello-client-resrc "travis"))

(comment
  (GET hello-client-resrc)
  (:body (GET hello-travis-client-resrc))
  (:body (GET hello-travis-client-resrc {:headers {"Accept" "text/plain"}}))
  (:body (GET hello-travis-client-resrc {:headers {"Accept" "text/html"}}))

  (time (dotimes [_ 100] (GET hello-travis-client-resrc {:headers {"Accept" "text/html"}}))))


