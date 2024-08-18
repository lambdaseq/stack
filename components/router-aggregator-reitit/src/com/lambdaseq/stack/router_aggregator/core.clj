(ns com.lambdaseq.stack.router-aggregator.core
  (:require [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.protocols.api.provider.http-router :as router]
            [com.stuartsierra.component :as component]
            [muuntaja.core :as m]
            [reitit.coercion.malli :as rcm]
            [reitit.ring :as ring]))

; TODO: Find a better way to inject underlying routers. This is a temporary solution.
(defrecord RouterAggregatorReitit [middleware router1 router2 router3 router4 router5 router]
  component/Lifecycle
  (start [this]
    (log/info! :starting-component {:component this})
    (-> this
        (assoc :router
               ; TODO: Investigate if `get-router` can be used instead.
               ;       According to the documentation, you can compose reitit routers like so,
               ;       but it did not work when tested.
               (ring/router [(when router1
                               (router/get-routes router1))
                             (when router2
                               (router/get-routes router2))
                             (when router3
                               (router/get-routes router3))
                             (when router4
                               (router/get-routes router4))
                             (when router5
                               (router/get-routes router5))]
                            {:conflicts (constantly nil)
                             :data      {:muuntaja   m/instance
                                         :coercion   rcm/coercion
                                         :middleware middleware}}))))
  (stop [this]
    (log/info! :stopping-component {:component this})
    (-> this
        (assoc :router1 nil)
        (assoc :router2 nil)
        (assoc :router3 nil)
        (assoc :router4 nil)
        (assoc :router5 nil)
        (assoc :router nil)))

  router/IHttpRouterProvider
  (get-router [_this] router)
  (get-routes [_this]
    (concat (router/get-routes router1)
            (router/get-routes router2)
            (router/get-routes router3)
            (router/get-routes router4)
            (router/get-routes router5))))

(defn make-router []
  (map->RouterAggregatorReitit {}))