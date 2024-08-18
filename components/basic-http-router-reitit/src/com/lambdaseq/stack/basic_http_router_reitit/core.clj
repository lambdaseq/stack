(ns com.lambdaseq.stack.basic-http-router-reitit.core
  (:require [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.protocols.api.provider.http-router :as router]
            [com.stuartsierra.component :as component]
            [muuntaja.core :as m]
            [reitit.coercion.malli :as rcm]
            [reitit.ring :as ring]))

(defrecord BasicHttpRouterReitit [middleware routes router]

  component/Lifecycle
  (start [this]
    (log/info! :starting-component {:component this})
    (assoc this :router
                (ring/router routes
                             {:conflicts (constantly nil)
                              :data      {:muuntaja   m/instance
                                          ; TODO: Coupling routers with malli coercion,
                                          ;       there should probably be a component that provides coercion protocol
                                          :coercion   rcm/coercion
                                          :middleware middleware}})))

  (stop [this]
    (log/info! :stopping-component {:component this})
    (-> this
        (assoc :middleware nil)
        (assoc :router nil)))

  router/IHttpRouterProvider
  (get-routes [_this] routes)
  (get-router [_this] router))

(defn make-router
  ([]
   (map->BasicHttpRouterReitit {}))
  ([routes]
   (map->BasicHttpRouterReitit {:routes routes})))