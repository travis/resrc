(ns resrc.test.representations
  (:use [resrc.representations])
  (:use [clojure.test]))

(deftest test-component-matches
  (is (component-matches :bar :*))
  (is (component-matches :* :*))
  (is (component-matches :foo :foo))
  (is (not (component-matches :foo :bar))))

(deftest test-type-matches
  (is (type-matches [:* :*] [:foo :bar]))
  (is (type-matches [:* :*] [:buz :bang]))
  (is (type-matches [:foo :*] [:foo :bar]))
  (is (type-matches [:foo :*] [:foo :baz]))
  (is (type-matches [:foo :bar] [:foo :bar]))

  (is (not (type-matches [:foo :bar] [:foo :baz])))
  (is (not (type-matches [:foo :bar] [:fuz :baz])))
  (is (not (type-matches [:foo :*] [:fuz :baz]))))

(deftest test-find-representation
  (let [f1 #(1)
        f2 #(2)
        f3 #(3)
        representations
          [[[:text :plain] f1]
           [[:text :html] f2]
           [[:image :jpeg] f3]]]
    (is (= [[:text :plain] f1] (find-representation [:* :*] representations)))
    (is (= [[:text :plain] f1] (find-representation [:text :*] representations)))
    (is (= [[:text :plain] f1] (find-representation [:text :plain] representations)))
    (is (= [[:text :html] f2] (find-representation [:text :html] representations)))
    (is (= [[:image :jpeg] f3] (find-representation [:image :jpeg] representations)))))

(deftest test-find-acceptable
    (let [representations
          [[[:text :plain] 1]
           [[:text :html] 2]
           [[:image :jpeg] 3]]]

      (is (= [[:text :plain] 1] (find-acceptable [[:* :*]] representations)))
      (is (= [[:text :plain] 1] (find-acceptable [[:text :plain]] representations)))
      (is (= [[:text :html] 2] (find-acceptable [[:text :html] [:* :*]] representations)))
      (is (= [[:image :jpeg] 3] (find-acceptable [[:image :jpeg] [:* :*]] representations)))))
