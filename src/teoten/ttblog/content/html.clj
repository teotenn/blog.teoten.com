(ns teoten.ttblog.content.html
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
            [teoten.ttblog.content.transformations :as trs])
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Document]))


(defn html-find-yml [html]
  (let [pattern #"(?s)<p>\s*(---|&#x2014;).*?</p>"  ;; Regex pattern
        match (re-find pattern html)]
    (if match
      (first match)  ;; Return the full match
      nil)))

(defn html-yml-to-map [yml-content]
  (let [lines (-> yml-content
                  (str/replace #"<p>|</p>" "")
                  (str/replace #"(?m)^---|&#x2014;" "")
                  (str/split-lines)
                  ((fn [s] (remove str/blank? s))))
        pairs (map #(str/split % #":\s*" 2) lines)]
    (into {}
          (map (fn [[k v]]
                 [(keyword (str/trim k))
                  (if (str/starts-with? v "[") ;; Check if it's a list
                    (edn/read-string v)
                    (str/trim v))])
               pairs))))

(defn prepare-html-page [p] 
  {:metadata (-> p html-find-yml html-yml-to-map)
   :head (trs/apply-jsoup-trans p trs/js-trans-extract-head)
   :body (-> p
             (trs/apply-hiccup-trans trs/hic-trans-img-node
                                     trs/hic-trans-add-class-to-headers)
             (trs/apply-jsoup-trans trs/js-trans-remove-yml
                                    trs/js-trans-extract-body-content))})
