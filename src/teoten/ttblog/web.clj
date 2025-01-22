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
            [teoten.ttblog.config :refer [app-env]])
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

(defn delete-parent-dirs
    "Removes all '../' parent directory references from the beginning of a given string.

  Parameters:
  - `s`: A string representing a file path, potentially containing '../' references.

  Returns:
  - A string with all '../' occurrences removed from the start of the input. 
    Any '../' references in the middle or end of the string are left untouched.

  Example usage:
  (delete-parent-dirs '../path/to/file)
  ;; => \"path/to/file\"
  "
  [s]
  (if (str/starts-with? s "../")
    (delete-parent-dirs (str/replace s #"\.\./" ""))
    s))


(defn regularize-link [relative-path? base-url]
  (let [static-dir (get-in @app-env [:directory :static] "static")
        path (-> relative-path?
                 delete-parent-dirs
                 (str/replace static-dir "")
                 (str/replace #"^/" ""))
        pre-url (if (and base-url (.endsWith base-url "/"))
                base-url
                (str (or base-url "") "/"))]
    (if (re-find #"^htt[p|ps]" path)
      path
      (str pre-url path))))


(defn apply-regularize-links [html-str]
  (let [base-url (if (= "server" (:function @app-env))
                   (str "http://localhost:" (:port @app-env) "/")
                   (:base-url @app-env))
        doc (Jsoup/parse html-str)  
        imgs (.select doc "img[src]")
        links (.select doc "a[href]")]
    (doseq [link links]
      (let [old-href (.attr link "href")
            new-href (regularize-link old-href base-url)] 
        (.attr link "href" new-href)))
    (doseq [img imgs]
      (let [old-src (.attr img "src")
            new-src (regularize-link old-src base-url)]  
        (.attr img "src" new-src))) 
    (.html doc)))



;; Preparing
(defn get-raw-pages []
  (let [regexp-ext (generate-extensions-regexp (get-in @app-env [:content-opts :extensions] ["org" "html" "md"]))]
    (stasis/merge-page-sources
     {:static
      (stasis/slurp-directory (glue-dir "static") #".*\.(css|js)$")
      :content
      (ce/post-pages! (stasis/slurp-directory (glue-dir "content") regexp-ext))
      :templates
      (temp/process-templates (glue-dir "templates"))
      :categories
      (temp/process-categories "categories")
      :tags
      (temp/process-categories "tags")
      :rss
      {"/index.xml" (rss/atom-xml @ce/posts-map)
       "/categories/r/index.xml" (rss/filter-rss-by-category (rss/atom-xml @ce/posts-map) "R")
       "/categories/web-dev/index.xml" (rss/filter-rss-by-category (rss/atom-xml @ce/posts-map) "web-dev")
       "/categories/clojure/index.xml" (rss/filter-rss-by-category (rss/atom-xml @ce/posts-map) "clojure")
       "/categories/emacs/index.xml" (rss/filter-rss-by-category (rss/atom-xml @ce/posts-map) "emacs")}
      ;; TODO: Change the categories here for a function that maps over all cats
      })))


(defn prepare-page [page req]
  (if (string? page) page (apply-regularize-links (page req))))

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





