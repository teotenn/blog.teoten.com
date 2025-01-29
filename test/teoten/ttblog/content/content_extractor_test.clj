(ns teoten.ttblog.content.content-extractor-test
  (:require [clojure.test :refer :all]
            [teoten.ttblog.content.content-extractor :refer :all]
            [clojure.string :as str]))

(deftest test-map-pages
    (testing "Processing pages with supported extensions"
      (let [pages-extracted {"file1.org" (str "#+draft: false\n#+title: Org Title\n"
                                              "#+date: 2024-01-01\n"
                                              "#+description: Description\nOrg content")
                             "file2.md" "Markdown content"
                             "file3.html" "HTML content"}
            app-env (atom {:content-opts {:build-drafts true}})
            result (map-pages pages-extracted "org")]

        ;; Validate the number of processed pages
        (is (= 1 (count result)))

        ;; Validate structure of a processed page
        (let [page (first result)]
          (is (= (:id page) "file1/"))
          (is (= (:path page) "file1/"))
          (is (= (:format page) "org"))
          (is (= (:metadata page) {:draft "false"
                                   :title "Org Title"
                                   :description "Description"
                                   :categories ["uncategorized"],
                                   :date "2024-01-01",
                                   :tags []
                                   :image "/img/default.jpg"}))
          (is (= (:head page) "")))))

    (testing "Filtering out drafts when build-drafts is false"
      (let [pages-extracted {"file1.org" (str "#+draft: true\n#+title: Org Title 1\n"
                                              "#+date: 2024-01-01\n"
                                              "#+description: Description\nOrg content")
                             "file2.org" (str "#+draft: false\n#+title: Org Title 2\n"
                                              "#+date: 2024-01-01\n"
                                              "#+description: Description\nOrg content")}
            app-env (atom {:content-opts {:build-drafts false}})
            result (map-pages pages-extracted "org")]
        (is (= 1 (count result)))))

    (testing "Unsupported extension throws an exception"
      (is (thrown-with-msg? Exception #"Unsupported extension: txt"
                            (map-pages {"file1.txt" "Some content"} "txt")))))

(deftest test-merge-vectors-with-unique-paths
  (testing "Merging vectors with unique paths"
    (let [x [{:path "/a" :data 1} {:path "/b" :data 2}]
          y [{:path "/c" :data 3}]
          result (merge-vectors-with-unique-paths x y)]
      (is (= (count result) 3))
      (is (= (set result) (set (concat x y))))))

  (testing "Merging vectors with no elements"
    (let [x []
          y []
          result (merge-vectors-with-unique-paths x y)]
      (is (empty? result)))))

(println "content-extractor-test loaded")
