(ns user
  (:require [teoten.ttblog.core :as co]))

(defn init []
  (co/-main {:env :server}))

(defn restart-server []
  (co/server-stop)
  (co/-main {:env :server}))

(def rs restart-server)

;; Mock a request to call function that need it
(def mock-request
  {:request-method :get
   :uri "http://localhost:3001"
   :headers {"Accept" "text/html"}
   :params {"key" "value"}
   :body nil})
