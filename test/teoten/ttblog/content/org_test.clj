(ns teoten.ttblog.content.org-test
  (:require [teoten.ttblog.content.org :refer :all]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(defn del-space [s]
  "Spaces resulting from Jsoup mess up everything, thus we are testing without spaces"
  (str/replace s #"\s+" ""))

(deftest test-prepare-org-page
  (testing "Parse org page to appropiate html"
    (let [org-txt "* Title\n** Subtitle\nText\n"
          expected (str "<div><h1class=\"article-header\">Title</h1>"
                        "<h2class=\"article-header\">Subtitle</h2><p>Text</p></div>")]
      (is (= expected (del-space (:body (prepare-org-page org-txt))))))

    (let [org-txt "* Title\n#+begin_src R\nfunction(x)print(x)\n#+end_src\n"
          expected (str "<div><h1class=\"article-header\">Title</h1>"
                        "<pre><codeclass=\"R\">function(x)print(x)</code></pre></div>")]
      (is (= expected (del-space (:body (prepare-org-page org-txt))))))))

(println "org-test loaded")
