(ns teoten.ttblog.cljs.filters
  (:require
   [reagent.core :as r]
   [reagent.dom  :as dom]))


;; Body
(defn overview []
  [:p "Hello, World!"])


;; Main functions to be called by the compiler
(defn app []
  [overview])

(defn ^:dev/after-load mount-components []
  (dom/render [#'app] (.getElementById js/document "filters-app")))


(defn init []
  (mount-components))
