(ns resrc.test.util
  (:use [resrc core identifiers representations util])
  (:use [clojure.test]))

(deftest test-process-request
  (let [resource
        (with-representations
          (reify Resource (GET [_ request] (str request "bar ")))
          [[[:text :plain] (fn [resource response] (str response "baz"))]])]
   (is (= "foo bar baz"
          (process-request (create-router [["/bar" resource]]) GET "/bar" [[:text :plain]] "foo ")))))


(deftest test-emit-resource-handler
  (is (= '(GET [+resource +request] foo)
         (emit-resource-handler '(GET foo)))))

(deftest test-emit-representations
  (is (= '[[[:text :plain] (clojure.core/fn [+resource +response] foo)]
           [[:text :html] (clojure.core/fn [+resource +response] bar)]]
           (emit-representations '[:text/plain foo
                                       :text/html bar]))))

(deftest test-resource
  (let [resource (resource
                  (GET "fuz ")
                  (PUT +request)
                  [:text/html (str +response "representation")])]
    (is (= "fuz representation"
           (process-request (create-router [["/bar" resource]])
                            GET "/bar" [[:text :html]] "foo ")))
    (is (= "foo representation"
           (process-request (create-router [["/bar" resource]])
                            PUT "/bar" [[:text :html]] "foo ")))))

