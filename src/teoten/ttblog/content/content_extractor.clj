(ns teoten.ttblog.content.content-extractor
  (:require [clojure.string :as str]
            [optimus.link :as link]
            [hiccup.page :refer [html5]]
            [teoten.ttblog.content.highlight :as hl]
            [teoten.ttblog.content.md :refer [prepare-md-page]]
            [teoten.ttblog.content.html :refer [prepare-html-page]]
            [selmer.parser :as selmer]
            [teoten.ttblog.config :refer [app-env]]
            [teoten.ttblog.content.org :refer [prepare-org-page]]))

(def posts-map (atom []))


;; Selmer support
(selmer/add-tag! :blog-content
    (fn [args context-map]
      (str "!!!--- ADD CONTENT,,,!!!")))

(selmer/add-tag! :blog-head
    (fn [args context-map]
      (str "!!!--- ADD HEAD,,,!!!")))


;; POSTS content extracted from mardkdown and org files
(defn wrap-post-as-article [p uid]
  (str "<article id=\"post-" uid "\" data-post-id=\"" uid "\">" p "</article>"))


(defn map-pages [pages-extracted extension]
  "Convert `pages` extracted from `stasis/slurp-directory` into a map with:
 `:id` Unique ID
 `:metadata` parsed from yml or org headers
 `:body` The content body. For now, without the layout page and syntax highlighting.
 `:path` The relative path to the file
 `:format` Origin format of the post (org or md)

`extension` is the file extension (org or md)."
  (let [xt (str/lower-case extension)]
    (if (not (some #(= % xt) ["org" "md" "html"]))
      (throw (Exception. (str "Unsupported extension: " extension)))
      (let [regexp (cond (= xt "org") #"\.org$"
                         (= xt "md") #"\.md$"
                         (= xt "html") #"\.html$")
            parse-f (cond (= xt "org") prepare-org-page
                          (= xt "md") prepare-md-page
                          (= xt "html") prepare-html-page)
            pages (if (get-in @app-env [:content-opts :build-drafts])
                    (filter (fn [[k _]] (str/ends-with? k (str "." xt))) pages-extracted)
                    (filter (fn [[k v]] (and
                                         (str/ends-with? k (str "." xt))
                                         (not (=  "true" (get-in (parse-f v) [:metadata :draft])))))
                            pages-extracted))]
        ;; Personal Note: Double [] above is to extract first the
        ;; vector, second the map _ is to ignore keys, as we only need
        ;; the values (entry)
        (vec (map (fn [[filename content]]
                       ;; Generate a unique ID
                       (let [uuid (str (random-uuid))
                             name (str/replace filename regexp  "/")
                             parsed-content (parse-f content)
                             title (get-in parsed-content [:metadata :title] "Quick Post")]
                         ;; Construct the new map entry
                         
                          {:id uuid
                           :metadata (:metadata parsed-content)
                           :head (:head parsed-content)
                           :body (let [p (:body parsed-content)]
                                   (-> p
                                       hl/highlight-code-blocks
                                       (wrap-post-as-article uuid)
                                       ((fn [b] (str "<h1 class=\"article-title\">" title "</h1>\n" b)))))
                           :path name
                           :format xt}))
                  pages))))))

(defn merge-vectors-with-unique-paths [x y]
  (let [merged (concat x y)
        paths (map :path merged)
        duplicates (->> paths
                        (frequencies)
                        (filter #(> (val %) 1))
                        (keys))]
    (if (seq duplicates)
      (throw (Exception. (str "Duplicate path(s) found: " duplicates)))
      merged)))


(defn register-pages! [pages-map ext]
  (swap! posts-map #(merge-vectors-with-unique-paths % (map-pages pages-map ext))))


(defn wrap-content-template [body head req]
  (let [template (get-in @app-env [:content-opts :content-template] "content.html")
        p (selmer/render-file template (merge {} {:req req}))]
    (-> p
        (str/replace "!!!--- ADD CONTENT,,,!!!" body)
        (str/replace "!!!--- ADD HEAD,,,!!!" head))))


(defn post-pages! [pages]
  (do
    (reset! posts-map [])
    (register-pages! pages "org")
    (register-pages! pages "md")
    (register-pages! pages "html")
    (zipmap (map :path @posts-map)
            (map #(fn [req] (wrap-content-template %1 %2 req))
                 (map :body @posts-map)
                 (map :head @posts-map)))))
