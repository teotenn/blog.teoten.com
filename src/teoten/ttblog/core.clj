(ns teoten.ttblog.core
  (:require [stasis.core :as stasis]
            [clojure.string :as str]
            [ring.adapter.jetty :as jetty]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.export]
            [teoten.ttblog.web :as w]
            [teoten.ttblog.config :as conf]))

;; Server
(defonce server (atom nil))

(defn server-start [port]
  (conf/load-specs!)
  (reset! server
          (jetty/run-jetty (fn [req] (w/app req))
                           {:port port
                            :join? false})))

(defn server-stop []
  (when-some [s @server]
    (.stop s)
    (reset! server nil)))


;; Build and export
(defn builder [export-dir]
  (conf/load-specs!)
  (let [assets (optimizations/all (w/get-assets) {})]
    (stasis/empty-directory! export-dir)
    (optimus.export/save-assets assets export-dir)
    (stasis/export-pages (w/get-pages) export-dir {:optimus-assets assets})))


(defn -main [{:keys [env]}]
  (conf/load-specs!)
  (conf/set-env! env)
  (let [config @conf/app-env
        behaviour (str/lower-case (:function config))]
    (cond (= behaviour "builder") (builder (:export-dir config))
          (= behaviour "server") (server-start (:port config))
          :else (throw (Exception. (str "Functionality not allowed: " behaviour))))))
