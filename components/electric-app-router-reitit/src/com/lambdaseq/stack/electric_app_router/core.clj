(ns com.lambdaseq.stack.electric-app-router.core
  (:require [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.protocols.api.provider.http-router :as router]
            [com.stuartsierra.component :as component]
            [muuntaja.core :as m]
            [reitit.coercion.malli :as rcm]
            [reitit.ring :as ring]))

(defn- -routes [handler]
  [["/app"
    ["*" {:get {:handler handler}}]]])

(defrecord ElectricAppRouter [electric-handler middleware routes router]
  component/Lifecycle
  (start [this]
    (log/info! :starting-component {:component this})
    (let [routes (-routes electric-handler)]
      (-> this
          (assoc :routes routes)
          (assoc :router
                 (ring/router routes
                              {:conflicts (constantly nil)
                               :data      {:muuntaja   m/instance
                                           :coercion   rcm/coercion
                                           :middleware middleware}})))))
  (stop [this]
    (log/info! :stopping-component {:component this})
    (-> this
        (assoc :electric-handler nil)
        (assoc :middleware nil)
        (assoc :routes nil)
        (assoc :router nil)))

  router/IHttpRouterProvider
  (get-routes [_this] routes)
  (get-router [_this] router))

(defn make-router []
  (map->ElectricAppRouter {}))