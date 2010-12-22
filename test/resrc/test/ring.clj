(ns resrc.test.ring
  (:use [resrc identifiers ring]
        [clojure.test])
  (:require [resrc.core :as core]))

(deftest test-parse-accept
  (is (= (seq [[:text :plain] [:text :html]])
         (parse-accept "text/plain, text/html")))
  ;; should sort by 'q' param
  (is (= (seq [[:text :plain] [:text :html]])
         (parse-accept "text/html;q=0.5, text/plain;q=0.8")))
  ;; q defaults to 1
  (is (= (seq [[:text :plain] [:text :html]])
         (parse-accept "text/html;q=0.5, text/plain"))))


(deftest test-request-processor
  (let [resource
        (resource
         (core/GET {:body "foo"}))]
    (is (= "foo"
           (:body ((request-processor (create-router [["/bar" resource]]))
                   {:request-method :get
                    :uri "/bar"
                    :headers {}}))))))

(deftest test-resource
  (let [resource (resource
                  (GET {:body "fuz "})
                  (PUT {:body +body})
                  [:text/html  {:body (str (:body +response) "representation")}])]
    (is (= "fuz representation"
           (:body ((ring-handler (create-router [["/bar" resource]]))
                   {:request-method :get
                    :uri "/bar"
                    :headers {"accept" "text/html"}
                    :body "foo "}))))
    (is (= "foo representation"
           (:body ((ring-handler (create-router [["/bar" resource]]))
                   {:request-method :put
                    :uri "/bar"
                    :headers {"accept" "text/html"}
                    :body "foo "}))))))


(deftest test-ring-handler
  (let [resource (resource
                  (GET {:body +body})
                  [:text/html  {:body (str (:body +response) "representation")}])]
    (is (= "foo representation"
           (:body ((ring-handler (create-router [["/bar" resource]]))
                   {:request-method :get
                    :uri "/bar"
                    :headers {"accept" "text/html"}
                    :body "foo "}))))
    (is (= 406
           (:status ((ring-handler (create-router [["/bar" resource]]))
                     {:request-method :get
                      :uri "/bar"
                      :headers {"accept" "text/plain"}}))))))



