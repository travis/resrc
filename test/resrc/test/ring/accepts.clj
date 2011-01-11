(ns resrc.test.ring.accepts
  (:use resrc.ring.accepts
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
