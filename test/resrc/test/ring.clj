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

(deftype foo []
  Resource
  (GET [r request] {:body "bar"}))

(defrepresentation bar [resource]
    (GET [repr request]
         (body-as
          :bork/plain (fn [body] "foo")
          :clam/plain (fn [body] "fun"))))

(deftest test-defrepresentation
  (is (= {:body "bar"} (GET (foo.) {})))
  (is (= {:headers {"Content-Type" "bork/plain"}, :body "foo"}
         (GET (bar. (foo.)) {:headers {"accept" "bork/plain"}})))
  (is (= {:headers {"Content-Type" "clam/plain"}, :body "fun"}
         (GET (bar. (foo.)) {:headers {"accept" "clam/plain"}}))))
