(ns resrc.test.ring
  (:use [resrc identifiers ring core]
        [clojure.test]))

(deftest test-handler
  (let [resource
        (reify Resource
         (GET [_ _] {:body "foo"}))]
    (is (= "foo"
           (:body ((handler (create-router [["/bar" resource]]))
                   {:request-method :get
                    :uri "/bar"
                    :headers {}}))))))
