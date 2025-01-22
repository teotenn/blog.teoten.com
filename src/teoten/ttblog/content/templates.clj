(ns teoten.ttblog.content.templates
  (:require [selmer.parser :as selmer]
            [clojure.string :as str]
            [optimus.link :as link]
            [teoten.ttblog.config :refer [app-env]]
            [teoten.ttblog.content.content-extractor :refer [posts-map]]
            [clj-time.coerce :as coerce]
            [clojure.java.io :as io]))

(selmer/set-resource-path!
 (io/resource (get-in @app-env [:directory :templates] "templates")))

(defn replace-missing-image [m]
  (let [d "img/default.jpg"
        img (get-in m [:metadata :image] d)
        render-img (if (= "" img) d img)]
    (update-in m [:metadata :image] (fn [_] render-img))))

(defn retrieve-content-info []
  (let [mm (vec (map
                (fn [v] (update-in v [:metadata :date] coerce/to-date))
                @posts-map))
        m (vec (map replace-missing-image mm))
        content (reverse (sort-by #(get-in % [:metadata :date]) m))]
    (vec (map #(select-keys % [:path :metadata]) content))))


(selmer/add-tag! :blog-styles
    (fn [args context-map]
      (str "<link href="
           (link/file-path (:req context-map) (first args))
           " rel=\"stylesheet\">")))

(selmer/add-tag! :blog-scripts
    (fn [args context-map]
      (str "<script src="
           (link/file-path (:req context-map) (first args))
           "></script>")))


;; Parse templates
(defn list-end-templates [path]
  (let [dir (io/file path)]
    (if (.isDirectory dir)
      (->> dir
           (.listFiles)
           (filter #(and (.isFile %) (.endsWith (str %) ".html")))
           (map #(.getName %))
           (map #(str "/" %)))
      (throw (Exception. (str dir " not found or not a directory."))))))


;; CATEGORIES AND TAGS
(defn reduce-metadata-lists
  "Reduces all the lists found in the metadata `element` to a single
  vector with unique values."
  [element]
  (let [values (map #(get-in % [:metadata element]) (retrieve-content-info))]
    (vec (reduce into #{} values))))


(defn filter-by-field [value key]
  "Filters a list of maps by a given category. Returns only the elements
   where the `:categories` vector from `:metadata` which contains the specified category.

   Parameters:
   - `category`: The category to search for (a string).

   Returns:
   - A list of maps containing the given category in their `:metadata :categories`."
  (filter #(some #{value} (get-in % [:metadata key])) (retrieve-content-info)))


(defn process-categories [category-name]
  (let [cats-list (reduce-metadata-lists (keyword category-name))]
    (zipmap (map #(str "/" category-name "/" % "/") cats-list)
            (map #(fn [req] (selmer/render-file
                             "partials/category_template.html"
                             (merge
                              {:req req}
                              {:category %}
                              {:content (filter-by-field % (keyword category-name))})))
                 cats-list))))


(defn map-supportive-info
  "Generates the map of supportive info to be used by the templates.

  `:categories` Vector. List of categories in content.
  `:tags` Vector. List of tags in content.
  "
  []
  {:categories (reduce-metadata-lists :categories)
   :tags (reduce-metadata-lists :tags)})



(defn process-templates [path]
  (let [html-files (list-end-templates path)
        templates-map (merge {:supportive-info (map-supportive-info)}
                             {:content (retrieve-content-info)})]
    (zipmap (map #(if (= % "/index.html") "/" (str/replace % #"\.html$" "/")) html-files)
            (map #(fn [req] (selmer/render-file % (merge templates-map {:req req}))) html-files))))
