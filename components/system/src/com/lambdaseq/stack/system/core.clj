(ns com.lambdaseq.stack.system.core
  (:require [com.lambdaseq.stack.api-router.api :as api-router]
            [com.lambdaseq.stack.domain.api :as domain]
            [com.lambdaseq.stack.electric-app-router.api :as electric-app-router]
            [com.lambdaseq.stack.entity-manager.api :as entity]
            [com.lambdaseq.stack.http-electric-handler.api :as http-electric-handler]
            [com.lambdaseq.stack.http-handler.api :as http-handler]
            [com.lambdaseq.stack.http-middleware.api :as http-middleware]
            [com.lambdaseq.stack.http-server.api :as http-server]
            [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.resource-router.api :as resource-router]
            [com.lambdaseq.stack.router-aggregator.api :as router-aggregator]
            [com.stuartsierra.component :as component]))

(defn start-system! [system]
  (when system
    (log/info! :starting-system {})
    (component/start-system system)))

(defn stop-system! [system]
  (when system
    (log/info! :stopping-system {})
    (component/stop-system system)))

; TODO: An example of a system map, add components according to the requirements of the system
(defn make-system [{:keys [name electric-entrypoint config]}]
  (let [{:keys [jetty hyperfiddle]} config]
    (component/system-map
      ::name name
      :jetty-config jetty
      :schemas domain/schemas
      :entity-manager (component/using
                        (entity/make-entity-manager)
                        [:schemas])

      :http-middleware http-middleware/middleware

      :http-electric-handler (http-electric-handler/electric-handler
                               electric-entrypoint
                               {:jetty       jetty
                                :hyperfiddle hyperfiddle})

      :api-router (component/using
                    (api-router/make-router [])
                    {:middleware :http-middleware})

      :electric-app-router (component/using
                             (electric-app-router/make-router)
                             {:electric-handler :http-electric-handler
                              :middleware       :http-middleware})

      :resource-router (component/using
                         (resource-router/make-router)
                         {:middleware :http-middleware
                          :config     :jetty-config})

      :http-router (component/using
                     (router-aggregator/make-router)
                     {:middleware          :http-middleware
                      :api-router          :api-router
                      :electric-app-router :electric-app-router
                      :resource-router     :resource-router})

      :http-handler (component/using
                      (http-handler/make-handler)
                      {:router-provider :http-router})

      :http-server (component/using
                     (http-server/make-server)
                     {:handler-provider :http-handler
                      :config           :jetty-config}))))