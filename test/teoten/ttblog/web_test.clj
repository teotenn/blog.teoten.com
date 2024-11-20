(ns teoten.ttblog.web-test
  (:require [teoten.ttblog.web :refer :all]
            [teoten.ttblog.test_utils :as utils]
            [clojure.test :refer :all]))


(use-fixtures :once utils/server-fixture)

(deftest pages-200-ok
  (testing "All pages respond with 200 OK"
    (doseq [url (keys (get-pages))]
      (when (some? url)
        #_(println "Testing URL:" url) ; Debugging: Ensure URL is not nil
        (let [response (app {:uri url})
              status (:status response)]
          #_(println "Response:" response) ; Debugging: Check the full response
          (is (= [url status] [url 200])))))))



(println "web-test loaded")
