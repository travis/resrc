(ns resrc.test.client
  (:use [resrc.core :only [GET]]
        [resrc.client])
  (:use [clojure.test]))

(defn url-echo-http-adapter
  [req]
  {:status 200
   :body (:url req)})

(def url-echo-resource
     (resource "http://example.com" url-echo-http-adapter))

(deftest resource-creation
  (is (= 200 (:status (GET url-echo-resource))))
  (is (= "http://example.com" (:body (GET url-echo-resource)))))

(def url-echo-subresource
     (subresource url-echo-resource "/foo"))

(deftest subresource-creation
  (is (= 200 (:status (GET url-echo-subresource))))
  (is (= "http://example.com/foo/bar"
         (:body (GET (subresource url-echo-subresource "bar")))))
  (is (= "http://example.com/foo/bar"
       (:body (GET (subresource url-echo-subresource "/bar")))))
  (is (= "http://example.com/foo/bar"
         (:body (GET (subresource url-echo-subresource "./bar")))))  )

