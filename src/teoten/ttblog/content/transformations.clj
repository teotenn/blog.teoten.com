(ns teoten.ttblog.content.transformations
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [hiccup.page :refer [html5]]
            [hickory.core :as hickory]
            [hickory.select :as select]
            [hickory.render :as render])
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Document]))

;; Function to process headers with p tag inside
(defn js-trans-extract-p-from-h [doc]
  "Extracts content of p tag when it is inside a header. Somehow this
  is the behaviour of the library `clj-org.org` and thus, we need to
  fix it.

  `dic` is a Jsoup document. This function is intended to work with `apply-jsoup-trans`"
  ;; Loop through all header tags (h1, h2, h3, h4, h5, h6) containing <p> tags
  (doseq [header (-> doc (.select "h1 > p, h2 > p, h3 > p, h4 > p, h5 > p, h6 > p"))]
    ;; Get the parent header element (h1, h2, etc.)
    (let [parent-header (.parent header)]
      ;; Extract the text from the <p> tag and set it as the header's new content
      (.text parent-header (.text header)))))

;; Function to change class name of <code> tags by removing the 'lang_' prefix
(defn js-trans-class-lang [doc]
  "Changes the class lang_language for language only. The function can
  takee multiple classes but it works only when lang_language is the
  first class. Since it is expected to work to parse org files, this
  behaviour should be enough.

  `dic` is a Jsoup document. This function is intended to work with `apply-jsoup-trans`"
  ;; Select all <code> tags that have a class attribute
  (doseq [code-tag (.select doc "code[class]")]
    (let [class-name (.attr code-tag "class")]
      ;; If the class starts with "lang_", replace it by removing the "lang_" part
      (when (.startsWith class-name "lang_")
        (.attr code-tag "class" (subs class-name 5))))))


(defn js-trans-extract-body-content [doc]
  (let [body (.body doc)
        body-html (.html body)]
    ;; Replace the document's HTML with only the body content
    (if (.startsWith (str/trim body-html) "<div>")
      (.html doc body-html)
      (.html doc (str "<div>" body-html "</div>")))))

(defn js-trans-extract-head [doc]
  (let [head (.head doc)
        head-html (.html head)]
    ;; Replace the document's HTML with only the head content
    (if (.startsWith (str/trim head-html) "<head>")
      (.html doc head-html)
      (.html doc (str "<head>" head-html "</head>")))))

(defn js-trans-remove-yml [doc]
  (let [paragraphs (.select doc "p")]
    ;; Iterate through each <p> tag
    (doseq [el paragraphs]
      (let [text (.text el)]
        (when (or (.startsWith text "---")
                  (.startsWith text "—")
                  (.startsWith text "&#x2014;"))
          (.remove el))))))



(defn apply-jsoup-trans [html-str & transformations]
  "Collects all jsoup `transformations` above and applies them to a `html-str`.

Since `jsoup` has its own class (jsoup doc), this function converts
the html string to it and back to string."
  (let [doc (Jsoup/parse html-str)]  ;; Parse the HTML
    ;; Apply each transformation function to the document
    (doseq [transformation transformations]
      (transformation doc))
    ;; Return the modified HTML as a string
    (.html doc)))



;; HICKORY AND HICCUP
(defn hic-trans-img-node
    "Wraps an img tag inside a div with class 'article-image-container' and a link to the image source."
  [node]
  (if (and (vector? node) (= :img (first node)))
    (let [src (get-in node [1 :src])]
      [:div {:class "article-image-container"}
       [:a {:href src} node]])
    node))


(defn hic-trans-add-class-to-headers
    "Adds a class 'header-class' to all header elements (h1 through h6)."
  [node]
  (let [header-tags #{:h1 :h2 :h3 :h4 :h5 :h6}]  ;; Set of all header tags
    (if (and (vector? node) (header-tags (first node)))
      (assoc-in node [1 :class] "article-header")
      node)))


(defn apply-hiccup-trans
  "Applies a series of `transformation` functions to a `hiccup-tree`.
  
   Takes the hiccup tree as the first argument and any number of
  transformation functions as the remaining arguments."
  [html-str & transformations]
  (let [hiccup-tree (map hickory/as-hiccup (hickory/parse-fragment html-str))]
    (html5
        (walk/postwalk #(reduce
                          (fn [tree trans-fn] (trans-fn tree))
                          % transformations)
                       hiccup-tree))))

