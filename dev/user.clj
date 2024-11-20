(ns user
  (:require [teoten.ttblog.core :as co]))

(defn init []
  (co/-main {:env :server}))

(defn restart-server []
  (co/server-stop)
  (co/-main {:env :server}))

(def rs restart-server)
