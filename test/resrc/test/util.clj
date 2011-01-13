(ns resrc.test.util
  (:use [resrc core util]
        [clojure.test]))

(deftype Roo []
  resrc.core.Resource
  (GET [t] "f")
  (GET [t l] "g"))

(defwrappertype RooToo [roo foo]
  resrc.core.Resource
  (GET [t] foo))

(def roo (Roo.))
(def rootoo (RooToo. roo "kleem!"))

(deftest defwrappertype-tests
  (is (= '((GET [t x] "g") (GET [l] "z"))
         (merge-method-defs
          '((GET [t] "f")
            (GET [t x] "g"))
          '((GET [l] "z")))))

  (is (= "f" (GET roo)))
  (is (= "kleem!" (GET rootoo)))
  (is (= "g" (GET roo nil)))
  (is (= "g" (GET rootoo nil))))


