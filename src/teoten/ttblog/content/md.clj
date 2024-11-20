(ns teoten.ttblog.content.md
  (:require [clojure.string :as str]
            [markdown.core :as md]
            [teoten.ttblog.content.transformations :as trs]))

;; Markdown pages
(defn parse-yml-metadata [ordered-map]
  "Parses an `ordered-map` as returned by the
  `md/md-to-html-string-with-meta` function, into a simple map in the
  same style of `parse-org-metadata`."
  (into {}
        (map (fn [[k v]]
               (if (coll? v)  ;; Check if the value is a collection
                 [k (mapv str v)]  ;; Convert collection values to a vector of strings
                 [k (str v)]))     ;; Convert single value to a string
             ordered-map)))

(defn prepare-md-page [p]
  (let [parsed-content (md/md-to-html-string-with-meta p)
        meta (parse-yml-metadata (parsed-content :metadata))
        html-str (parsed-content :html)]
    {:metadata meta
     :head (trs/apply-jsoup-trans html-str trs/js-trans-extract-head)
     :body (-> html-str
               (trs/apply-hiccup-trans trs/hic-trans-img-node
                                       trs/hic-trans-add-class-to-headers)
               (trs/apply-jsoup-trans trs/js-trans-extract-body-content))}))
