(ns teoten.ttblog.config
  (:require [cprop.core :refer [load-config]]))

;; Supportive funcs
(defn resolve-inheritance [maps m-key]
  "Resolves inheritance in internal `maps` defined by `m-key`. In this
  case, `maps` are the internal maps inside the config environments
  and `m-key` is an environment key.

It resolves recursively to ensure that inheritance is resolved first."
  (let [m (get maps m-key)
        inherit-k (:inherit m)]
    (if inherit-k
      (if-let [inherited-map (get maps inherit-k)]
        (merge (resolve-inheritance maps inherit-k) (dissoc m :inherit))
        (throw (Exception.
                (str "Error: Inherited env " inherit-k " not found for " m-key))))
      m)))

(defn resolve-all-inheritances [maps]
  "Applies `resolve-inheritance to `maps`."
  (reduce-kv
   (fn [acc k v] (assoc acc k (resolve-inheritance maps k)))
   {} maps))


;; STATE
(def config-specs (atom {}))
(def app-env (atom {}))

(defn set-env! [env]
  (reset! app-env (get @config-specs (keyword env))))

(defn load-specs! []
  (reset! config-specs (resolve-all-inheritances (load-config))))

(defn set-build-drafts-val! [v]
  (reset! app-env (update-in @app-env [:content-opts :build-drafts] (fn [_] (boolean v)))))
