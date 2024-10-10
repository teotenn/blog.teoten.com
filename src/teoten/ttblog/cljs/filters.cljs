(ns teoten.ttblog.cljs.filters
  (:require
   [ajax.core :refer [GET POST]]
   [reagent.core :as r]
   [reagent.dom  :as dom]))


(def message (r/atom nil))

(defn con-handler [response]
  (do
    (reset! message response)
    (.log js/console (str response))))

(defn con-error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn fetch-greeting []
    (GET "https://test.teoten.com/site_map.json"
         {;; :headers {"Partial Content" "text/xml"}
          :format "application/json"
          :response-format {206 "Partial Content"}
          :handler con-handler
          :error-handler con-error-handler})
    (fn []
      [:div
       [:h1 "ClojureScript Frontend"]
       [:p "Greeting from backend api: " @message]]))

;; Body
(defn overview []
  [:div
   [:p "Hello, World!!! "]
   [fetch-greeting]])


;; Main functions to be called by the compiler
(defn app []
  [overview])

(defn ^:dev/after-load mount-components []
  (dom/render [#'app] (.getElementById js/document "filters-app")))


(defn init []
  (mount-components))
