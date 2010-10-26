(ns resrc.test.identifiers
  (:use [resrc.identifiers])
  (:use [clojure.test]))

(deftest test-find-resource
  (let [router (create-router
                [["/foo" 1]
                 ["/foo/:id" 2]])]
    (is (= [1 {}] (router "/foo")))
    (is (= [2 {"id" "bar"}] (router "/foo/bar")))
    (is (= nil (router "/bar")))))
