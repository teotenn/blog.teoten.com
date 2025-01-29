(ns teoten.ttblog.content.rss
  (:require
   [clojure.data.xml :as xml]
   [clj-time.coerce :as coerce]
   [teoten.ttblog.config :refer [app-env]]))

(defn get-base-url []
  (if (= "server" (:function @app-env))
    (str "http://localhost:" (:port @app-env))
    (:base-url @app-env)))

(defn entry [post]
  (let [base-url (get-base-url)
        path (str base-url (:path post))]
    [:item
     [:title (get-in post [:metadata :title])]
     [:link {:href path} path]
     [:pubDate (coerce/to-date (get-in post [:metadata :date]))]
     [:author "Teoten"]
     [:guid (:id path)]
     [:categories (map #(str % ", ") (get-in post [:metadata :categories]))]
     [:description {:type "html"} (:body post)]]))

(defn atom-xml [posts]
  (let [base-url (get-base-url)
        sorted-posts (sort-by #(get-in % [:metadata :date]) #(compare %2 %1) posts)]
    (xml/emit-str
     (xml/sexp-as-element
      [:rss
       [:channel
        [:title "Teoten's blog"]
        [:link {:rel "self" :href (str base-url "/index.xml/") :type "application/rss+xml"}
         base-url]
        [:description "Recent content on Teoten's blog"]
        [:language "en"]
        [:updated (-> posts first (get-in [:metadata :date]) coerce/to-date)]
        [:lastBuildDate (new java.util.Date)]
        (map entry sorted-posts)]]))))


(defn sitemap-element [post]
  (let [base-url (get-base-url)
        path (str base-url (:path post))]
    [:url
     [:loc path]
     [:lastmod (get-in post [:metadata :date])]]))

(defn sitemap-xml [posts]
  (let [base-url (get-base-url)
        sorted-posts (sort-by #(get-in % [:metadata :date]) #(compare %2 %1) posts)]
    (xml/emit-str
     (xml/sexp-as-element
      [:urlset {:xmlns "http://www.sitemaps.org/schemas/sitemap/0.9"}
       (map sitemap-element sorted-posts)]))))


;; XML by Category
(defn item-has-category? [item category]
  "Helper function to check if an `item` contains a specific `category`."
  (some #(re-find (re-pattern category) (first (:content %)))
        (filter #(= :categories (:tag %))
                (:content item))))

(defn filter-rss-by-category [rss-feed category]
  "Filter an `rss-feed` ITEM that contains `category` in CATEGORIES tag."
  (let [rss (xml/parse-str rss-feed) ; Parse the XML string
        channel (first (filter #(= :channel (:tag %)) (:content rss))) ; Extract the <channel> element
        items (filter #(= :item (:tag %)) (:content channel)) ; Get all <item> elements
        filtered-items (filter #(item-has-category? % category) items) ; Filter items by category
        new-channel (assoc channel :content (concat (remove #(= :item (:tag %)) (:content channel)) filtered-items))] ; Rebuild the <channel> with filtered items
    (xml/emit-str (assoc rss :content [new-channel])))) ; Rebuild the entire RSS with the new <channel>
;; TODO: Add error handers in the function above
;; For example, when a post has not the required fields in metradata, this function simply breaks
