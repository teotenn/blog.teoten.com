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

(deftest test-generate-extensions-regexp
  (testing "Regex pattern for file extensions"
    (let [pattern (generate-extensions-regexp ["jpg" "png" "gif"])]
      ;; Ensure the pattern matches the expected filenames
      (is (re-matches pattern "image.jpg"))
      (is (re-matches pattern "photo.png"))
      (is (re-matches pattern "animation.gif"))
      
      ;; Ensure the pattern does not match non-matching filenames
      (is (nil? (re-matches pattern "document.pdf")))
      (is (nil? (re-matches pattern "archive.zip")))
      (is (nil? (re-matches pattern "file.jpg.doc")))
      
      ;; Ensure it works with a single extension
      (let [single-ext-pattern (generate-extensions-regexp ["txt"])]
        (is (re-matches single-ext-pattern "notes.txt"))
        (is (nil? (re-matches single-ext-pattern "notes.doc")))))))

(deftest test-delete-parent-dirs
  (testing "Removing '../' from the start of file paths"
    ;; Normal cases
    (is (= (delete-parent-dirs "../file.txt") "file.txt"))
    (is (= (delete-parent-dirs "../../folder/file.txt") "folder/file.txt"))
    (is (= (delete-parent-dirs "../..") ".."))

    ;; No '../' at the start
    (is (= (delete-parent-dirs "folder/file.txt") "folder/file.txt"))
    (is (= (delete-parent-dirs "path/../file.txt") "path/../file.txt"))

    ;; Empty string input
    (is (= (delete-parent-dirs "") ""))))


(println "web-test loaded")
