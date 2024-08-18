(ns com.lambdaseq.stack.admin-base.main
  (:require [hyperfiddle.electric :as e]
            [com.lambdaseq.stack.bootstrap.core :refer [bootstrap-system!]]
            [com.lambdaseq.stack.admin-base.app :as entrypoint])
  (:gen-class))

(defn server-entrypoint [req]
  (e/boot-server {} entrypoint/App req))

(defn start! []
  (bootstrap-system! {:name                "Admin System"
                      :config-path         "admin-base/config.edn"
                      :routes              []
                      :electric-entrypoint server-entrypoint}))

(defn -main [& args]
  (start!))
