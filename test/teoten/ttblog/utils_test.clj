(ns teoten.ttblog.utils-test
  (:require [teoten.ttblog.utils :refer :all]
            [clojure.test :refer :all]))

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

(println "utils-test loaded")
