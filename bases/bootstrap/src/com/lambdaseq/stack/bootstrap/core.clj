(ns com.lambdaseq.stack.bootstrap.core
  (:require [com.lambdaseq.stack.config.api :as config]
            [com.lambdaseq.stack.system.api :as system]))

(defn bootstrap-system!
  [{:keys [name config-path routes electric-entrypoint]}]
  (let [config (config/load-config! config-path)
        system (system/make-system {:name                name
                                    :electric-entrypoint electric-entrypoint
                                    :routes              routes
                                    :config              config})]
    (system/start-system! system)))
