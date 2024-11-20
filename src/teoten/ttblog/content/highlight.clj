(ns teoten.ttblog.content.highlight
  (:require [clojure.java.io :as io]
            [clygments.core :as pygments]
            [net.cgrand.enlive-html :as enlive]))

(defn extract-code
  [highlighted]
  (when (some? highlighted)
    (-> highlighted
        java.io.StringReader.
        enlive/html-resource
        (enlive/select [:pre])
        first
        :content)))


(defn high-light [node]
  (let [code (->> node :content (apply str))
        lang (->> node :attrs :class keyword)]
    (assoc node :content (-> code
                             (pygments/highlight lang :html)
                             extract-code))))


(defn highlight-code-blocks [page]
  (enlive/sniptest page
            [:pre :code] high-light
            [:pre :code] #(assoc-in % [:attrs :class] "highlight")))
