(ns hello.core
  (:use [resrc core identifiers ring]
        [ring.adapter.jetty :only [run-jetty]]
        [ring.middleware.params :only [wrap-params]])
  (:require [resrc.client :as client]))

(def hello-server-resrc (resource (GET {:status 200 :body "hello"})))

(def hello-travis-server-resrc
     (resource (GET {:status 200
                     :body (str "hello " (+params "name"))})
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
                ["/hello/:name" hello-travis-server-resrc]]))
             wrap-params))

(defn -main [] (run-jetty #'app {:port 8080}))

(def hello-client-resrc (client/resource "http://localhost:8080/hello"))
(def hello-travis-client-resrc (client/subresource hello-client-resrc "travis"))

(comment
  (GET hello-client-resrc)
  (:body (GET hello-travis-client-resrc {:query-params {"foo" "bar" "fuz" "baz"} :headers {"Accept" "text/html"}})))
