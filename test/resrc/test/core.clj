(ns resrc.test.core
  (:use [resrc.core])
  (:use [clojure.test]))


(deftest test-Resource
  (let [r (reify Resource
                 (GET [_ _] "fun"))]
    (is (= "fun" (GET r nil)))))

(extend-protocol Resource
  clojure.lang.APersistentMap
  (GET [hash request] (get hash request)))

(deftest test-extended-hash
  (is (= :bar (GET {:foo :bar} :foo))))

(extend-protocol Resource Integer (GET [i _] (.intValue i)))

(deftest test-integer
  (is (= 1 (GET (Integer. "1") nil))))
