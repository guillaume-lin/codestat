(ns codestat.core-test
  (:require [clojure.test :refer :all]
            [codestat.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

(deftest b-test
  (testing "fail?"
           (is (= (* 2 3) 7))))