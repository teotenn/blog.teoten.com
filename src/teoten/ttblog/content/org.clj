(ns teoten.ttblog.content.org
  (:require [clojure.string :as str]
            [hiccup.page :refer [html5]]
            [clj-org.org :refer [parse-org]]
            [teoten.ttblog.content.transformations :as trs]))


;; Org pages
(defn parse-org-line [l]
  "Parses single `l` of org file metadata. The line should start with
  #+. It also parses the collections of values into clj vectors."
  (when-let [[_ key value] (re-matches #"\#\+([A-Za-z]+):\s*(.*)" l)]
    [(keyword (str/lower-case key))
     (if (re-find #"\[.*\]" value) (read-string value) value)]))

(defn parse-org-metadata [input-str]
  "Parses the org metadata from `input-str` (string of lines) to a
  simple map in the same style of `parse-yml-metadata`."
  (let [lines (str/split-lines input-str)]
    (into {} (keep parse-org-line lines))))  ;; Build the map, ignoring nil results


(defn prepare-org-page [p]
  (let [parsed-content (parse-org p)
        meta (parse-org-metadata (parsed-content :headers))
        html-str (str/replace (html5 (parsed-content :content)) #"<!DOCTYPE html>\n" "")]
    {:metadata meta
     :head (trs/apply-jsoup-trans html-str trs/js-trans-extract-head)
     :body (-> html-str
               (trs/apply-hiccup-trans trs/hic-trans-img-node
                                       trs/hic-trans-add-class-to-headers)
               (trs/apply-jsoup-trans trs/js-trans-extract-p-from-h
                                      trs/js-trans-class-lang
                                      trs/js-trans-extract-body-content))}))
