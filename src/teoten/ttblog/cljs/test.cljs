(ns teoten.ttblog.cljs.test
  (:require [goog.dom :as gdom]))

(defn init []
  (let [button (gdom/getElement "testing-js")]
    (.addEventListener button "click" #(js/alert "JS Works!"))))

;; Call the init function to attach the event listener when the page loads
(init)
