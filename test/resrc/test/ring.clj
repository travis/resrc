(ns resrc.test.ring
  (:use [resrc identifiers ring]
        [clojure.test])
  (:require [resrc.core :as core]
            [resrc.util :as util]))

(deftest test-parse-accept
  (is (= (seq [[:text :plain] [:text :html]])
         (parse-accept "text/plain, text/html")))
  ;; should sort by 'q' param
  (is (= (seq [[:text :plain] [:text :html]])
         (parse-accept "text/html;q=0.5, text/plain;q=0.8")))
  ;; q defaults to 1
  (is (= (seq [[:text :plain] [:text :html]])
         (parse-accept "text/html;q=0.5, text/plain"))))


(deftest test-process-request
  (let [resource
        (util/resource
         (core/GET "foo ")
         [:text/plain {:body (str +response "bar")}])]
    (is (= "foo bar"
           (:body (process-request (create-router [["/bar" resource]])
                                   {:request-method :get
                                    :uri "/bar"
                                    :headers {"accept" "text/plain"}}))))
    (is (= 406
           (:status (process-request (create-router [["/bar" resource]])
                                     {:request-method :get
                                      :uri "/bar"
                                      :headers {"accept" "text/html"}}))))))

(deftest test-resource
  (let [resource (resource
                  (GET "fuz ")
                  (PUT +body)
                  [:text/html  {:body (str +response "representation")}])]
    (is (= "fuz representation"
           (:body (process-request (create-router [["/bar" resource]])
                                   {:request-method :get
                                    :uri "/bar"
                                    :headers {"accept" "text/html"}
                                    :body "foo "}))))
    (is (= "foo representation"
           (:body (process-request (create-router [["/bar" resource]])
                                   {:request-method :put
                                    :uri "/bar"
                                    :headers {"accept" "text/html"}
                                    :body "foo "}))))))


(deftest test-resource
  (let [resource (resource
                  (GET +body)
                  [:text/html  {:body (str +response "representation")}])]
    (is (= "foo representation"
           (:body (process-request (create-router [["/bar" resource]])
                                   {:request-method :get
                                    :uri "/bar"
                                    :headers {"accept" "text/html"}
                                    :body "foo "}))))))
