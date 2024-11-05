(ns com.lambdaseq.stack.resources-router.core
  (:require [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.protocols.api.provider.http-router :as router]
            [com.stuartsierra.component :as component]
            [muuntaja.core :as m]
            [reitit.coercion.malli :as rcm]
            [reitit.ring :as ring]
            [ring.util.http-response :as http]))

(defrecord ResourceRouterReitit [middleware config routes router]
  component/Lifecycle
  (start [this]
    (log/info! :starting-component {:component this})
    (let [{:keys [resources-path]} config
          routes [["/js/*" (ring/create-resource-handler
                             {:root              (str resources-path "/js")
                              :not-found-handler (constantly http/not-found)})]
                  ["/css/*" (ring/create-resource-handler
                              {:root              (str resources-path "/css")
                               :not-found-handler (constantly http/not-found)})]
                  ["/public/*" (ring/create-resource-handler
                                 {:root              resources-path
                                  :not-found-handler (constantly http/not-found)})]]
          router (ring/router routes
                              {:conflicts (constantly nil)
                               :data      {:muuntaja   m/instance
                                           :coercion   rcm/coercion
                                           :middleware middleware}})]
      (-> this
          (assoc :routes routes)
          (assoc :router router))))

  (stop [this]
    (log/info! :stopping-component {:component this})
    (-> this
        (assoc :middleware nil)
        (assoc :config nil)
        (assoc :routes nil)
        (assoc :router nil)))

  router/IHttpRouterProvider
  (get-router [_this] router)
  (get-routes [_this] routes))

(defn make-router []
  (map->ResourceRouterReitit {}))
