(ns teoten.ttblog.cljs.test
  (:require [goog.dom :as gdom]))

(defn init []
  (let [button (gdom/getElement "testing-cljs")]
    (.addEventListener button "click" #(js/alert "My cljs too?"))))

;; Call it with buton ID testing-cljs
;; <button id="testing-cljs">Testing</button>

;; Call the init function to attach the event listener when the page loads
(init)
