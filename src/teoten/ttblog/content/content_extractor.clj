(ns teoten.ttblog.content.content-extractor
  (:require [clojure.string :as str]
            [optimus.link :as link]
            [hiccup.page :refer [html5]]
            [cheshire.core :as json]
            [teoten.ttblog.content.highlight :as hl]
            [teoten.ttblog.content.md :refer [prepare-md-page]]
            [teoten.ttblog.content.html :refer [prepare-html-page]]
            [selmer.parser :as selmer]
            [teoten.ttblog.config :refer [app-env]]
            [teoten.ttblog.utils :as utils]
            [teoten.ttblog.content.org :refer [prepare-org-page]]))

(def posts-map
  "Vector of maps, one map per content element. Each containing the following:
  `:id` Identifier, thus, relative path.
  `:metadata` All the metadata from yml in md or metadata in org
  files. In html takes yml as the first paragraph.
  `:head` If an hatml `<head>` section exists, its content is stored here.
  `:body` The hamlt `<body>` section.
  `:path` Relative path to the file -> website.
  `:format` of the source file.
  "
  (atom []))


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

;; JSON
(defn normalize-namespaced-keys [clojure-map]
  "Converts namespaced keys like :ld/context to @context."
  (let [transform-key (fn [k]
                        (if (and (keyword? k) (= "ld" (namespace k)))
                          (str "@" (name k)) ;; Replace :ld/context -> @context
                          k))]
    (clojure.walk/postwalk
     (fn [x]
       (if (map? x)
         (into {} (map (fn [[k v]] [(transform-key k) v]) x))
         x))
     clojure-map)))


(defn schema-to-json-ld [schema-map]
  "Converts a prepared schema map to a JSON-LD string."
  (json/generate-string (normalize-namespaced-keys schema-map)))

(defn create-schema-map-from-post-map [post-map]
  (let [metadata (:metadata post-map)
        base-url (get @app-env :base-url "")]
    {:ld/context "https://schema.org"
     :ld/type "BlogPosting"
     :articleSection "Programming"
     :isPartOf {:ld/type "Blog"
                :name (get @app-env :blog-name "")
                :url base-url}
     :headline (:title metadata)
     :datePublished (utils/format-google-datetime (:date metadata))
     :dateModified (utils/format-google-datetime (get metadata :modified (:date metadata)))
     :author {:ld/type "Person"
              :name (:author metadata)
              :url (get-in @app-env [:schema-markup :personal-url])
              :sameAs (get-in @app-env [:schema-markup :socials])}
     :description (get metadata :description "")
     :keywords (str/join ", " (get metadata :tags ["programming" "software"]))
     :mainEntityOfPage {:ld/type "WebPage"
                        :ld/id (str base-url (:path post-map))}
     :image (utils/regularize-link (:image metadata) base-url)
     :about (vec (map #(hash-map :ld/type "Thing" :name %) (:categories metadata)))
     :inLanguage (get metadata :language "en")
     :publisher {:ld/type "Person"
                 :name "teoten"}}))

(defn schema-to-html-str [schema-map]
  (str "<script type=\"application/ld+json\">"
       (schema-to-json-ld schema-map)
       "</script>"))
;; --


(defn standardize-metadata
  "Standardizes the following fields in the map `m` (ideally metadata
  section) when missing or empty: `image`, `draft`, `tags` and
  `categories`.

  Additionally, throws informative errors when missing `title`,
  `description` and `date`, for the last one also when format is not
  acceptable. The errors are rendered by `selmer` in server mode."
  [m]
  (let [m-image (if (= "" (get m :image ""))
                  (get @app-env :default-img  "/img/default.jpg")
                  (:image m))
        m-draft (get m :draft true)
        m-tags (get m :tags [])
        m-cats (get m :categories ["uncategorized"])
        raw-date (get m :date "")]
    (if (= "" (get m :title "")) (throw (Exception. (str "Post without a title: " m))))
    (if (= "" (get m :description ""))
      (throw (Exception. (str "Post \"" (:title m) "\" needs a description."))))
    (if (= "" raw-date)
      (throw (Exception. (str "Post \"" (:title m) "\" missing date.")))
      (if (not (re-matches #"[0-9]{4}-[0-1][0-9]-[0-3][0-9]" raw-date))
        (throw (Exception. (str "Wrong date format in \"" (:title m) "\".")))))
    (-> m
        (assoc :image m-image)
        (assoc :draft m-draft)
        (assoc :tags m-tags)
        (assoc :categories m-cats))))


(defn map-pages
  "Convert `pages` extracted from `stasis/slurp-directory` into a map with:
 `:id` Unique ID
 `:metadata` parsed from yml or org headers
 `:body` The content body. For now, without the layout page and syntax highlighting.
 `:path` The relative path to the file
 `:format` Origin format of the post (org or md)

`extension` is the file extension (org or md)."
  [pages-extracted extension]
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
                    (let [name (str/replace filename regexp  "/")
                          uuid name
                          parsed-content (parse-f content)
                          title (get-in parsed-content [:metadata :title] "Quick Post")
                          metadata (standardize-metadata (:metadata parsed-content))
                          ;; metadata
                          #_(if (= "" (get raw-metadata :image ""))
                                     (update raw-metadata :image
                                             (fn [_] (get @app-env :default-img  "/img/default.jpg")))
                                     raw-metadata)]
                      ;; Construct the new map entry
                      {:id uuid
                       :metadata metadata
                       :head (:head parsed-content)
                       :body (let [p (:body parsed-content)]
                               (-> p
                                   hl/highlight-code-blocks
                                   (wrap-post-as-article uuid)))
                       :path name
                       :format xt}))
                  pages))))))

(defn merge-vectors-with-unique-paths
  "Merges two vectors of maps, ensuring each map has a unique `:path` key.
  
  Each map in the vectors is expected to have a `:path` key. If any duplicate 
  `:path` values are found across the two vectors, an exception is thrown.
  
  Returns:
  - A single vector containing all maps from `x` and `y` if no duplicates are found."
  [x y]
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


(defn wrap-content-template [post-map req]
  (let [template (get-in @app-env [:content-opts :content-template] "content.html")
        p (selmer/render-file template (merge post-map {:req req}))]
    (-> p
        (str/replace "!!!--- ADD CONTENT,,,!!!" (:body post-map))
        (str/replace "!!!--- ADD HEAD,,,!!!" (str (:head post-map)
                                                  (schema-to-html-str
                                                   (create-schema-map-from-post-map post-map)))))))


(defn post-pages! [pages]
  (do
    (reset! posts-map [])
    (register-pages! pages "org")
    (register-pages! pages "md")
    (register-pages! pages "html")
    (zipmap (map :path @posts-map)
            (map #(fn [req] (wrap-content-template % req)) @posts-map))))


;; TODO: in wrap-content-template change `schema` for a real schema based on the content map. Create a fun for that
