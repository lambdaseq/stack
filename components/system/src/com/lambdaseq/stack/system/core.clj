(ns com.lambdaseq.stack.system.core
  (:require [com.stuartsierra.component :as component]
            [com.lambdaseq.stack.domain.api :as domain]
            [com.lambdaseq.stack.email-client-mailgun.api :as mailgun.email]
            [com.lambdaseq.stack.entity-manager.api :as entity]
            [com.lambdaseq.stack.http-electric-handler.api :as http-electric-handler]
            [com.lambdaseq.stack.http-handler.api :as http-handler]
            [com.lambdaseq.stack.http-middleware.api :as http-middleware]
            [com.lambdaseq.stack.http-router.api :as http-router]
            [com.lambdaseq.stack.http-server.api :as http-server]
            [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.migration-datomic.api :as datomic.migration]
    ;[com.lambdaseq.stack.persistence-xtdb.api :as xtdb]
            [com.lambdaseq.stack.persistence-datomic-pro.api :as datomic-pro]
            [com.lambdaseq.stack.persistence-schema-transformer-malli-datomic.api :as m.d.persistence-schema-transformer]))

(defn start-system! [system]
  (when system
    (log/info! :starting-system {})
    (component/start-system system)))

(defn stop-system! [system]
  (when system
    (log/info! :stopping-system {})
    (component/stop-system system)))

(defn make-system [{:keys [name entrypoint routes config]}]
  (let [{:keys [jetty datomic hyperfiddle]} config]
    (component/system-map
      ::name name

      :jetty-config jetty
      :datomic-config datomic
      :schemas domain/schemas
      :entity-manager (component/using
                        (entity/make-entity-manager)
                        [:schemas])

      :email-client-config {}
      :email-client (component/using
                      (mailgun.email/make-email-client)
                      {:config :email-client-config})

      :datomic-persistence-schema-transformer (component/using
                                                (m.d.persistence-schema-transformer/make-persistence-schema-transformer)
                                                [:entity-manager])

      :datomic-migration (component/using
                           (datomic.migration/make-migration)
                           {:entity-manager          :entity-manager
                            :persistence-transformer :datomic-persistence-schema-transformer})

      :datomic-persistence (component/using
                             (datomic-pro/make-persistence)
                             {:entity-manager :entity-manager
                              :config         :datomic-config})
      :http-middleware http-middleware/middleware
      :http-electric-handler (http-electric-handler/electric-handler entrypoint {:jetty       jetty
                                                                                 :hyperfiddle hyperfiddle})
      :http-routes routes
      :http-router (component/using
                     (http-router/make-router)
                     {:middleware       :http-middleware
                      :electric-handler :http-electric-handler
                      :routes           :http-routes
                      :config           :jetty-config})
      :http-handler (component/using
                      (http-handler/make-handler)
                      {:router-provider :http-router})
      :http-server (component/using
                     (http-server/make-server)
                     {:handler-provider :http-handler
                      :config           :jetty-config}))))


