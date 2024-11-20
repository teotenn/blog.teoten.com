(ns teoten.ttblog.test_utils
  (:require [teoten.ttblog.core :as co]))

(defn server-fixture [f]
  (co/-main {:env :test})

  (try
    (f)
    (finally
      (co/server-stop))))
