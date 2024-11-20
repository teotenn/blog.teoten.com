(ns teoten.ttblog.content.md-test
  (:require [teoten.ttblog.content.md :refer :all]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(defn del-space [s]
  "Spaces resulting from Jsoup mess up everything, thus we are testing without spaces"
  (str/replace s #"\s+" ""))


(deftest test-prepare-md-page
  (testing "Parse markdown page to appropiate html"
    (let [md-txt "# Title\n## Subtitle\nText\n"
          expected (str "<div><h1class=\"article-header\">Title</h1>"
                        "<h2class=\"article-header\">Subtitle</h2><p>Text</p></div>")]
      (is (= expected (del-space (:body (prepare-md-page md-txt))))))

    (let [md-txt "# Title\n```R\nfunction(x)print(x)\n```\n"
          expected (str "<div><h1class=\"article-header\">Title</h1>"
                        "<pre><codeclass=\"R\">function(x)print(x)</code></pre></div>")]
      (is (= expected (del-space (:body (prepare-md-page md-txt))))))))

(println "md-test loaded")
