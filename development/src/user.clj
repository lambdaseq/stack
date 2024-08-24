(ns user
  (:require [clojure.tools.namespace.repl :as tools.repl]
            [com.lambdaseq.stack.admin-base.main :as admin]
            [com.lambdaseq.stack.api-router.api :as api-router]
            [com.lambdaseq.stack.client-base.main :as client]
            [com.lambdaseq.stack.domain.api :as domain]
            [com.lambdaseq.stack.electric-app-router.api :as electric-app-router]
            [com.lambdaseq.stack.email-client-mailgun.api :as mailgun.email]
            [com.lambdaseq.stack.entity-manager.api :as entity]
            [com.lambdaseq.stack.http-electric-handler.api :as http-electric-handler]
            [com.lambdaseq.stack.http-handler.api :as http-handler]
            [com.lambdaseq.stack.http-middleware.api :as http-middleware]
            [com.lambdaseq.stack.http-server.api :as http-server]
            [com.lambdaseq.stack.migration-datomic.api :as datomic.migration]
            [com.lambdaseq.stack.persistence-datomic-pro.api :as datomic-pro]
            [com.lambdaseq.stack.persistence-schema-transformer-malli-datomic.api :as m.d.persistence-schema-transformer]
            [com.lambdaseq.stack.repl.core :as repl]
            [com.lambdaseq.stack.resource-router.api :as resource-router]
            [com.lambdaseq.stack.router-aggregator.api :as router-aggregator]
            [com.lambdaseq.stack.system.api :as system]
            [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [hashp.core]
            [shadow.cljs.devtools.api :as shadow.api]
            [shadow.cljs.devtools.server :as shadow.server]))

(repl/start-nrepl!)

(defonce system
         (let [admin-jetty-config {:port           8080
                                   :join?          false
                                   :resources-path "admin/public"
                                   :public-path    "/public"}
               client-jetty-config {:port           8081
                                    :join?          false
                                    :resources-path "client/public"
                                    :public-path    "/public"}
               admin-hyperfiddle {:manifest-path                     "admin/public/js/manifest.edn"
                                  :hyperfiddle.electric/user-version "dev"}
               client-hyperfiddle {:manifest-path                     "admin/public/js/manifest.edn"
                                   :hyperfiddle.electric/user-version "dev"}]

           (component/system-map
             :xtdb-config {}
             :admin-jetty-config admin-jetty-config
             :client-jetty-config client-jetty-config
             :datomic-config {:uri "datomic:mem://dev"}
             :schemas domain/schemas
             :email-client-config {}
             :email-client (component/using
                             (mailgun.email/make-email-client)
                             {:config :email-client-config})
             :entity-manager (component/using
                               (entity/make-entity-manager)
                               [:schemas])

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

             :admin-electric-handler (http-electric-handler/electric-handler
                                       admin/server-entrypoint
                                       {:jetty       admin-jetty-config
                                        :hyperfiddle admin-hyperfiddle})

             :client-electric-handler (http-electric-handler/electric-handler
                                        client/server-entrypoint
                                        {:jetty       client-jetty-config
                                         :hyperfiddle client-hyperfiddle})

             :admin-api-router (component/using
                                 (api-router/make-router [])
                                 {:middleware :http-middleware})

             :admin-electric-router (component/using
                                      (electric-app-router/make-router)
                                      {:electric-handler :admin-electric-handler
                                       :middleware       :http-middleware})

             :admin-resource-router (component/using
                                      (resource-router/make-router)
                                      {:middleware :http-middleware
                                       :config     :admin-jetty-config})

             :admin-router (component/using
                             (router-aggregator/make-router)
                             {:middleware :http-middleware
                              :router1    :admin-api-router
                              :router2    :admin-electric-router
                              :router3    :admin-resource-router})

             :client-api-router (component/using
                                  (api-router/make-router [])
                                  {:middleware :http-middleware})

             :client-electric-router (component/using
                                       (electric-app-router/make-router)
                                       {:electric-handler :client-electric-handler
                                        :middleware       :http-middleware})

             :client-resource-router (component/using
                                       (resource-router/make-router)
                                       {:middleware :http-middleware
                                        :config     :client-jetty-config})

             :client-router (component/using
                              (router-aggregator/make-router)
                              {:middleware :http-middleware
                               :router1    :client-api-router
                               :router2    :client-electric-router
                               :router3    :client-resource-router})

             :admin-handler (component/using
                              (http-handler/make-handler)
                              {:router-provider :admin-router})

             :client-handler (component/using
                               (http-handler/make-handler)
                               {:router-provider :client-router})

             :admin-server (component/using
                             (http-server/make-server)
                             {:handler-provider :admin-handler
                              :config           :admin-jetty-config})

             :client-server (component/using
                              (http-server/make-server)
                              {:handler-provider :client-handler
                               :config           :client-jetty-config}))))

(defn start! []
  (alter-var-root #'system system/start-system!))

(defn stop! []
  (alter-var-root #'system system/stop-system!))

(defn go []
  (start!))

(defn halt []
  (stop!))

(defn reset []
  (halt)
  ;(deps/sync-deps)
  (tools.repl/refresh-all :after 'user/go))

(defn refresh []
  (tools.repl/refresh-all))

(defn start-shadow! []
  (shadow.server/start!)
  (shadow.api/watch :admin-dev)
  (shadow.api/watch :client-dev))

(defn stop-shadow! []
  (shadow.server/stop!))

(defn restart-shadow! []
  (stop-shadow!)
  (start-shadow!))


(comment
  (def datomic (:datomic-persistence admin-system))

  ;; add a person to datomic
  (let [{:keys [conn]} datomic
        person {:first-name "John" :last-name "Doe" :age 30}]
    (d/transact (:conn datomic) [{:com.lambdaseq.stack.protocols.api.persistence/schema :person
                                  :com.lambdaseq.stack.protocols.api.persistence/data   person}]))
  )