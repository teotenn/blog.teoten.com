(ns teoten.ttblog.web
  (:require [stasis.core :as stasis]
            [clojure.string :as str]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer [serve-live-assets]]
            [teoten.ttblog.content.templates :as temp]
            [teoten.ttblog.content.content-extractor :as ce]
            [teoten.ttblog.content.rss :as rss]
            [clojure.data.xml :as xml]
            [teoten.ttblog.config :refer [app-env]]
            [teoten.ttblog.utils :as utils])
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Document]))

(defn generate-extensions-regexp
  "Generates a regex pattern that matches file extensions from a given collection.

  Parameters:
  - `exts`: A collection of file extensions (strings) without leading dots. 
            For example, `['jpg', 'png', 'gif']`.

  Returns:
  - A compiled regex pattern that matches filenames ending with any of the provided extensions.
    The regex will look like `.*\\.(ext1|ext2|...)$` where `ext1`, `ext2`, etc., are the input extensions.

  Example usage:
  (generate-extensions-regexp ['jpg 'png])
  ;; => #\".*\\.(jpg|png)$\"
  "
  [exts]
  (let [joined-extensions (clojure.string/join "|" exts)
        pattern (re-pattern (str ".*\\." "(" joined-extensions ")$"))]
    (re-pattern pattern)))

(defn glue-dir [dir]
  (let [env @app-env]
    (str "resources/" (get-in env [:directory (keyword dir)] dir) "/")))



;; Preparing
(defn get-raw-pages []
  (let [regexp-ext (generate-extensions-regexp (get-in @app-env [:content-opts :extensions] ["org" "html" "md"]))]
    (stasis/merge-page-sources
     {:static
      (stasis/slurp-directory (glue-dir "static") #".*\.(css|js|txt)$")
      :content
      (ce/post-pages! (stasis/slurp-directory (glue-dir "content") regexp-ext))
      :templates
      (temp/process-templates (glue-dir "templates"))
      :categories
      (temp/process-categories "categories" (get-in @app-env [:content-opts :category-list-template]))
      :tags
      (temp/process-categories "tags" (get-in @app-env [:content-opts :category-list-template]))
      :rss
      {"/index.xml" (rss/atom-xml @ce/posts-map)
       "/sitemap.xml" (rss/sitemap-xml @ce/posts-map)
       "/categories/r/index.xml" (rss/filter-rss-by-category (rss/atom-xml @ce/posts-map) "R")
       "/categories/web-dev/index.xml" (rss/filter-rss-by-category (rss/atom-xml @ce/posts-map) "web-dev")
       "/categories/clojure/index.xml" (rss/filter-rss-by-category (rss/atom-xml @ce/posts-map) "clojure")
       "/categories/emacs/index.xml" (rss/filter-rss-by-category (rss/atom-xml @ce/posts-map) "emacs")}
      ;; TODO: Change the categories here for a function that maps over all cats
      })))


(defn prepare-page [page req]
  (if (string? page) page (utils/apply-regularize-links (page req))))

(defn prepare-pages [pages]
  (zipmap (keys pages)
          (map #(partial prepare-page %) (vals pages))))

(defn get-pages [] 
  (prepare-pages (get-raw-pages)))


;; Optimizations
(defn get-assets []
  (assets/load-assets (get-in @app-env [:directory :static] "static") [#".*"]))

(def app
  (optimus/wrap (stasis/serve-pages get-pages)
                get-assets
                optimizations/all
                serve-live-assets))





