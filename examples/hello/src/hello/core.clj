(ns hello.core
  (:use [resrc core identifiers ring]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.params :only [wrap-params]]
        [ring.util.response :only [response]])
  (:require [resrc.client :as client]))

(def hello-server-resrc (resource (GET (response "hello"))))

(def hello-name-server-resrc
     (resource (GET (response (str "hello " (+params "name"))))
               [:text/plain +response
                :text/html (assoc +response
                             :body (str "<html>"
                                        "<body>"
                                        (:body +response)
                                        "</body>"
                                        "</html>"))]))

(def app (-> (ring-handler
              (create-router
               [["/hello" hello-server-resrc]
                ["/hello/:name" hello-name-server-resrc]]))
             wrap-params))

(defn -main [] (run-jetty #'app {:port 8080}))

(def hello-client-resrc (client/resource "http://localhost:8080/hello"))
(def hello-travis-client-resrc (client/subresource hello-client-resrc "travis"))

(comment
  (GET hello-client-resrc)
  (:body (GET hello-travis-client-resrc))
  (:body (GET hello-travis-client-resrc {:headers {"Accept" "text/html"}})))
