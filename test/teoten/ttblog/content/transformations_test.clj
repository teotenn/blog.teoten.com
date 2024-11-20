(ns teoten.ttblog.content.transformations-test
  (:require [teoten.ttblog.content.transformations :refer :all]
            [clojure.string :as str]
            [clojure.test :refer :all]))

(defn del-space [s]
  "Spaces resulting from Jsoup mess up everything, thus we are testing without spaces"
  (str/replace s #"\s+" ""))


(deftest test-extract-p-from-h
  (testing "Extract text from <p> tags inside headers and replace as header content."
    
    (let [input-html "<div><h1><p>A title</p></h1><h2><p>A subtitle</p></h2></div>"
          expected-html (str "<html><head></head><body>"
                             "<div><h1>Atitle</h1><h2>Asubtitle</h2></div>"
                             "</body></html>")]
      (is (= expected-html (del-space (apply-jsoup-trans input-html js-trans-extract-p-from-h)))))

    (let [input-html "<div><h1>No content here</h1><h2>No content either</h2></div>"
          expected-html (str "<html><head></head><body>"
                             "<div><h1>Nocontenthere</h1><h2>Nocontenteither</h2></div>"
                             "</body></html>")]
      (is (= expected-html (del-space (apply-jsoup-trans input-html js-trans-extract-p-from-h)))))

    (let [input-html "<div><h1><p>Title inside</p></h1><h2>Subtitle outside</h2></div>"
          expected-html (str "<html><head></head><body>"
                             "<div><h1>Titleinside</h1><h2>Subtitleoutside</h2></div>"
                             "</body></html>")]
      (is (= expected-html (del-space (apply-jsoup-trans input-html js-trans-extract-p-from-h)))))

    (let [input-html "<div><h1><div class=\"weird\">Inside a div</div></h1></div>"
          expected-html (str "<html><head></head><body>"
                             "<div><h1><divclass=\"weird\">Insideadiv</div>""</h1></div>"
                             "</body></html>")]
      (is (= expected-html (del-space (apply-jsoup-trans input-html js-trans-extract-p-from-h)))))))

(deftest test-class-lang
  (testing "Extract lang_ from a class inside code tag."
    
    (let [input-html "<code class=\"lang_r\">function(x)print(x)</code>"
          expected-html (str "<html><head></head><body>"
                             "<codeclass=\"r\">function(x)print(x)</code>"
                             "</body></html>")]
      (is (= expected-html (del-space (apply-jsoup-trans input-html js-trans-class-lang)))))

    (let [input-html "<code class=\"r\">function(x)print(x)</code>"
          expected-html (str "<html><head></head><body>"
                             "<codeclass=\"r\">function(x)print(x)</code>"
                             "</body></html>")]
      (is (= expected-html (del-space (apply-jsoup-trans input-html js-trans-class-lang)))))

    (let [input-html "<code class=\"lang_r more\">function(x)print(x)</code>"
          expected-html (str "<html><head></head><body>"
                             "<codeclass=\"rmore\">function(x)print(x)</code>"
                             "</body></html>")]
      (is (= expected-html (del-space (apply-jsoup-trans input-html js-trans-class-lang)))))))


(println "transformations-test loaded")
