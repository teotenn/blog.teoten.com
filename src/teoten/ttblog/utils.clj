(ns teoten.ttblog.utils
  (:require [clojure.string :as str]
            [teoten.ttblog.config :refer [app-env]])
  (:import [org.jsoup Jsoup]
           [org.jsoup.nodes Document]))


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

