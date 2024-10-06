(ns teoten.ttblog.config-test
  (:require [clojure.test :refer :all]
            [teoten.ttblog.config :refer :all]))

(def env-maps
  {:dev {:num 1
         :txt "Aveces"
         :vec ["A" "Z"]
         :func println}
   :test {:inherit :dev
          :num 23
          :new "Not in dev"}
   :prod {:inherit :test}
   :personal {:num 12
              :vec ["B" "X"]
              :func print}})

(deftest test-resolve-inheritance
  ;; Testing the resolve-inheritance function with valid inheritance
  (testing "Resolve inheritance for :test, inheriting from :dev"
    (let [result (resolve-inheritance env-maps :test)]
      (is (= result {:num 23
                     :new "Not in dev"
                     :txt "Aveces"
                     :vec ["A" "Z"]
                     :func println}))))

  ;; Testing resolve-inheritance with invalid inheritance
  (testing "Resolve inheritance with a missing inherited key"
    (let [invalid-envs (assoc env-maps :test {:inherit :non-existent})]
      (is (thrown? Exception
                   (resolve-inheritance invalid-envs :test))))))

(deftest test-resolve-all-inheritances
  ;; Testing the resolve-all-inheritances function for all maps
  (testing "Resolve all inheritances"
    (let [result (resolve-all-inheritances env-maps)]
      ;; Test for :dev (no inheritance)
      (is (= (get result :dev) {:num 1
                                :txt "Aveces"
                                :vec ["A" "Z"]
                                :func println}))
      ;; Test for :test (inheriting from :dev)
      (is (= (get result :test) {:num 23
                                 :new "Not in dev"
                                 :txt "Aveces"
                                 :vec ["A" "Z"]
                                 :func println}))
      ;; Test for :prod (inheriting from :test)
      (is (= (get result :prod) {:num 23
                                 :new "Not in dev"
                                 :txt "Aveces"
                                 :vec ["A" "Z"]
                                 :func println}))
      ;; Test for :personal (no inheritance)
      (is (= (get result :personal) {:num 12
                                     :vec ["B" "X"]
                                     :func print})))))

(println "config-test loaded")
