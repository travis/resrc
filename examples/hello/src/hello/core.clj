(ns hello.core
  (:use [resrc core identifiers ring]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.params :only [wrap-params]]
        [ring.util.response :only [response]]
        [hiccup.core :only [html]])
  (:require [resrc.client :as client]
            [resrc.representations :as repr]))

(extend-type Object
  Resource
  (GET [o req] (response (.toString o))))

(extend-type java.util.Map
  Resource
  (GET [m req] (response m))
  Representable
  (represent [m accepts response]
             ((represent-body-fn-from
               :text/plain (fn [body] (str body))
               :text/html (fn [body]
                            (html
                             [:dl
                              (apply concat
                                     (for [[k v] body]
                                       [[:dt k] [:dd v]]))])))
              m accepts response)))

(def hello-server-resrc (resource (GET (response "hello"))))

(def hello-name-server-resrc
     (resource (GET (response (str "hello " (+params "name"))))
               (body-as
                :text/plain identity
                :text/html (fn [body] (html [:div body])))))

(def app (-> (ring-handler
              (create-router
               [["/hello" hello-server-resrc]
                ["/hello/:name" hello-name-server-resrc]
                ["/class" Class]
                ["/map" {:a :b :c :d}]]))
             wrap-params))

(defn -main [] (run-jetty #'app {:port 8080}))

(def hello-client-resrc (client/resource "http://localhost:8080/hello"))
(def hello-travis-client-resrc (client/subresource hello-client-resrc "travis"))

(comment
  (GET hello-client-resrc)
  (:body (GET hello-travis-client-resrc))
  (:body (GET hello-travis-client-resrc {:headers {"Accept" "text/html"}})))
