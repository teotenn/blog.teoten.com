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


(defn process-templates [path]
  (let [html-files (list-end-templates path)
        templates-map (merge {:to-be-determine "NOT USED YET"}
                                        ; FIXME: NOT USED YET in the line above
                             {:content (retrieve-content-info)})]
    (zipmap (map #(if (= % "/index.html") "/" (str/replace % #"\.html$" "/")) html-files)
            (map #(fn [req] (selmer/render-file % (merge templates-map {:req req}))) html-files))))
